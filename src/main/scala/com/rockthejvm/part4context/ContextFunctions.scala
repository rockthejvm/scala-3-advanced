package com.rockthejvm.part4context

import scala.concurrent.{ExecutionContext, Future}

object ContextFunctions {

  val aList = List(2,1,3,4)
  val sortedList = aList.sorted

  // defs can take using clauses
  def methodWithoutContextArguments(nonContextArg: Int)(nonContextArg2: String): String = ???
  def methodWithContextArguments(nonContextArg: Int)(using nonContextArg2: String): String = ???

  // eta-expansion
  val functionWithoutContextArguments = methodWithoutContextArguments
  // val func2 = methodWithContextArguments // doesn't work

  // context function
  val functionWithContextArguments: Int => String ?=> String = methodWithContextArguments

  /*
    Use cases:
      - convert methods with using clauses to function values
      - HOF with function values taking given instances as arguments
      - requiring given instances at CALL SITE, not at DEFINITION
   */
  // execution context here
  // val incrementAsync: Int => Future[Int] = x => Future(x + 1) // doesn't work without a given EC in scope

  val incrementAsync: ExecutionContext ?=> Int => Future[Int] = x => Future(x + 1)

  def main(args: Array[String]): Unit = {

  }
}
