package com.rockthejvm.part5ts

import scala.util.Try

object HigherKindedTypes {

  class HigherKindedType[F[_]] // hkt
  class HigherKindedType2[F[_], G[_], A]

  val hkExample = new HigherKindedType[List]
  val hkExample2 = new HigherKindedType2[List, Option, String]
  // can use hkts for methods as well

  // why: abstract libraries, e.g. Cats
  // example: Functor
  val aList = List(1,2,3)
  val anOption = Option(2)
  val aTry = Try(42)

  val anIncrementedList = aList.map(_ + 1) // List(2,3,4)
  val anIncrementedOption = anOption.map(_ + 1) // Some(3)
  val anIncrementedTry = aTry.map(_ + 1) // Success(43)

  // "duplicated" APIs
  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def do10xTry(theTry: Try[Int]): Try[Int] = theTry.map(_ * 10)

  // DRY principle
  // step 1: TC definition
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  // step 2: TC instances
  given listFunctor: Functor[List] with
    override def map[A, B](list: List[A])(f: A => B): List[B] = list.map(f)

  // step 3: "user-facing" API
  def do10x[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ * 10)

  // if you create TC instances for Option and Try, the do10x method will work on Option and Try too

  // step 4: extension methods
  extension [F[_], A](container: F[A])(using functor: Functor[F])
    def map[B](f: A => B): F[B] = functor.map(container)(f)

  def do10x_v2[F[_] : Functor](container: F[Int]): F[Int] =
    container.map(_ * 10) // map is an extension method

  /**
   * Exercise: implement a new type class on the same structure as Functor.
   * In the general API, must use for-comprehensions.
   */
  def combineList[A, B](listA: List[A], listB: List[B]): List[(A, B)] =
    for {
      a <- listA
      b <- listB
    } yield (a, b)

  def combineOption[A, B](optionA: Option[A], optionB: Option[B]): Option[(A, B)] =
    for {
      a <- optionA
      b <- optionB
    } yield (a, b)

  def combineTry[A, B](tryA: Try[A], tryB: Try[B]): Try[(A, B)] =
    for {
      a <- tryA
      b <- tryB
    } yield (a, b)


  /*
    def combine[F[_]: SomeTC, A, B](fa: F[A], fb: F[B]): F[(A,B)] =
      for {
        a <- fa
        b <- fb
      } yield (a, b)
   */

  // 1 - TC definition
  trait Monad[F[_]] extends Functor[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  // 2 - TC instance(s)
  given magicList: Monad[List] with {
    override def map[A, B](list: List[A])(f: A => B) = list.map(f)
    override def flatMap[A, B](list: List[A])(f: A => List[B]) = list.flatMap(f)
  }

  // 3 - "user-facing" API
  def combine[F[_], A, B](fa: F[A], fb: F[B])(using magic: Monad[F]): F[(A, B)] =
    magic.flatMap(fa)(a => magic.map(fb)(b => (a,b)))
    // listA.map(a => listB.map(b => (a,b))

  extension [F[_], A](container: F[A])(using magic: Monad[F])
    def flatMap[B](f: A => F[B]): F[B] = magic.flatMap(container)(f)

  def combine_v2[F[_] : Monad, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    for {
      a <- fa
      b <- fb
    } yield (a, b)

  def main(args: Array[String]): Unit = {
    println(do10x(List(1,2,3)))
  }
}
