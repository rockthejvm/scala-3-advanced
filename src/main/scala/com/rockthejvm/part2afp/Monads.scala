package com.rockthejvm.part2afp

import scala.annotation.targetName

object Monads {

  def listStory(): Unit = {
    val aList = List(1,2,3)
    val listMultiply = for {
      x <- List(1,2,3)
      y <- List(4,5,6)
    } yield x * y
    // for comprehensions = chains of map + flatMap
    val listMultiply_v2 = List(1,2,3).flatMap(x => List(4,5,6).map(y => x * y))

    val f = (x: Int) => List(x, x + 1)
    val g = (x: Int) => List(x, 2 * x)
    val pure = (x: Int) => List(x) // same as the list "constructor"

    // prop 1: left identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for every x, for every f

    // prop 2: right identity
    val rightIdentity = aList.flatMap(pure) == aList // for every list

    // prop 3: associativity
    /*
      [1,2,3].flatMap(x => [x, x+1]) = [1,2,2,3,3,4]
      [1,2,2,3,3,4].flatMap(x => [x, 2*x]) = [1,2, 2,4,    2,4, 3,6,     3,6, 4,8]
      [1,2,3].flatMap(f).flatMap(g) = [1,2, 2,4, 2,4, 3,6, 3,6, 4,8]

      [1,2,2,4] = f(1).flatMap(g)
      [2,4,3,6] = f(2).flatMap(g)
      [3,6,4,8] = f(3).flatMap(g)
      [1,2, 2,4, 2,4, 3,6, 3,6, 4,8] = f(1).flatMap(g) ++ f(2).flatMap(g) ++ f(3).flatMap(g)
      [1,2,3].flatMap(x => f(x).flatMap(g))
     */
    val associativity = aList.flatMap(f).flatMap(g) == aList.flatMap(x => f(x).flatMap(g))
  }

  def optionStory(): Unit = {
    val anOption = Option(42)
    val optionString = for {
      lang <- Option("Scala")
      ver <- Option(3)
    } yield s"$lang-$ver"
    // identical
    val optionString_v2 = Option("Scala").flatMap(lang => Option(3).map(ver => s"$lang-$ver"))

    val f = (x: Int) => Option(x + 1)
    val g = (x: Int) => Option(2 * x)
    val pure = (x: Int) => Option(x) // same as Option "constructor"

    // prop 1: left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for any x, for any f

    // prop 2: right-identity
    val rightIdentity = anOption.flatMap(pure) == anOption // for any Option

    // prop 3: associativity
    /*
      anOption.flatMap(f).flatMap(g) = Option(42).flatMap(x => Option(x + 1)).flatMap(x => Option(2 * x)
      = Option(43).flatMap(x => Option(2 * x)
      = Option(86)

      anOption.flatMap(x => f(x).flatMap(g)) = Option(42).flatMap(x => Option(x + 1).flatMap(y => 2 * y)))
      = Option(42).flatMap(x => 2 * x + 2)
      = Option(86)
     */
    val associativity = anOption.flatMap(f).flatMap(g) == anOption.flatMap(x => f(x).flatMap(g)) // for any option, f and g
  }

  // MONADS = chain dependent computations

  // exercise: IS THIS A MONAD?
  // answer: IT IS A MONAD!
  // interpretation: ANY computation that might perform side effects
  case class IO[A](unsafeRun: () => A) {
    def map[B](f: A => B): IO[B] =
      IO(() => f(unsafeRun()))

    def flatMap[B](f: A => IO[B]): IO[B] =
      IO(() => f(unsafeRun()).unsafeRun())
  }

  object IO {
    @targetName("pure")
    def apply[A](value: => A): IO[A] =
      new IO(() => value)
  }

  def possiblyMonadStory(): Unit = {
    val aPossiblyMonad = IO(42)
    val f = (x: Int) => IO(x + 1)
    val g = (x: Int) => IO(2 * x)
    val pure = (x: Int) => IO(x)

    // prop 1: left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42)

    // prop 2: right-identity
    val rightIdentity = aPossiblyMonad.flatMap(pure) == aPossiblyMonad

    // prop 3: associativity
    val associativity = aPossiblyMonad.flatMap(f).flatMap(g) == aPossiblyMonad.flatMap(x => f(x).flatMap(g))

    println(leftIdentity)
    println(rightIdentity)
    println(associativity)
    println(IO(3) == IO(3))
    // ^^ false negative.

    // real tests: values produced + side effect ordering
    val leftIdentity_v2 = pure(42).flatMap(f).unsafeRun() == f(42).unsafeRun()
    val rightIdentity_v2 = aPossiblyMonad.flatMap(pure).unsafeRun() == aPossiblyMonad.unsafeRun()
    val associativity_v2 = aPossiblyMonad.flatMap(f).flatMap(g).unsafeRun() == aPossiblyMonad.flatMap(x => f(x).flatMap(g)).unsafeRun()

    println(leftIdentity_v2)
    println(rightIdentity_v2)
    println(associativity_v2)

    val fs = (x: Int) => IO {
      println("incrementing")
      x + 1
    }

    val gs = (x: Int) => IO {
      println("doubling")
      x * 2
    }

    val associativity_v3 = aPossiblyMonad.flatMap(fs).flatMap(gs).unsafeRun() == aPossiblyMonad.flatMap(x => fs(x).flatMap(gs)).unsafeRun()
  }

  def possiblyMonadExample(): Unit = {
    val aPossiblyMonad = IO {
      println("printing my first possibly monad")
      // do some computations
      42
    }

    val anotherPM = IO {
      println("my second PM")
      "Scala"
    }

    val aForComprehension = for { // computations are DESCRIBED, not EXECUTED
      num <- aPossiblyMonad
      lang <- anotherPM
    } yield s"$num-$lang"
  }

  def main(args: Array[String]): Unit = {
    possiblyMonadStory()
  }
}
