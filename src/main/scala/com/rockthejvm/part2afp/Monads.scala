package com.rockthejvm.part2afp

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


  def main(args: Array[String]): Unit = {

  }
}
