package com.rockthejvm.part1advancedscala

import scala.annotation.tailrec

object Recap {

  // values, types, expressions
  val aCondition = false // vals are constants
  val anIfExpression = if (aCondition) 42 else 55 //expressions evaluate to a value

  // value of a code block is the value of its last expression
  val aCodeBlock = {
    if (aCondition) 54
    78
  }

  // types: Int, String, Double, Boolean, Char
  // Unit - similar to void in java
  val theUnit = println("Hello, Scala!")

  // functions
  def aFunction(x: Int): Int = x + 1

  // recursion: stack & tail
  @tailrec
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, n* acc)

  val fac10 = factorial(10, 1)

  // OO programming
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  // similar to Java interface
  // this trait is an abstract data type
  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("CHOMP CHOMP")
  }

  // method notation
  val aCrocodile = new Crocodile
  aCrocodile.eat(aDog)
  aCrocodile eat aDog // infix position

  // anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("I'm a carnovire!")
  }

  // generics
  abstract class LinkedList[A] {
    // type A is known inside the implementation here
  }

  // singletons and companions
  object LinkedList // companion obj - obj of the same name, in the same file
  // used for instance independant fields, method - like java static

  // case class
  // apply method - so no new method :-)
  // toString - lots for free
   case class Person(name: String, age: Int)

  // enums - only scala 3?
  enum BasicColours {
    case RED, GREEN, BLUE
  }

  val red = BasicColours.RED

  // exceptions, try, catch, finally
  def throwSomeException(): Int = {
    throw new RuntimeException
  }

  val aPotentialFail = try {
    throwSomeException()
  } catch {
    case e: Exception => println("OOPS")
  } finally {
    // closing resources
    println("some important logs etc")
  }

  // functional programming
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = x + 1
  }

  val two = incrementer.apply(2)
  val three = incrementer(3)

  // lambda
  val anonIncrementer = (x: Int) => x + 1
  val anonResult = anonIncrementer(2)

  // higher order functions
  val anIncrementedList = List(1,2,3).map(anonIncrementer)

  // for comprehensions
  val forComp = for {
    number <- List(1,2,3)
    char <- List('a', 'b', 'c')
  } yield (number, char)

  // Scala collections: Seqs, Arrays, Lists, Vectors, Maps, Tuples, Sets

  // Options / Try
  val anOption: Option[Int] = Option(42)

  // pattern matching
  val x = 2
  var order = x match {
    case 1 => "first"
    case 2 => "second"
    case _ => "not important"
  }

  val bob = Person("bob", 12)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
  }

  // braceless syntax - indentation replaces {}
  val pairs_v2 =
    for
      number <- List(1,2,3)
      char <- List('a', 'b', 'c')
    yield (number, char)

  // same for if, match and while loops

  // indentation tokens
  class BracelessAnimal: // <- colon here - indentation replaces {}
    def eat(): Unit =
      println("I'm eating")
      println("I'm still eating")

  class BracelessAnimal2: // even more unusual syntax :-/
    def eat(): Unit =
      println("I'm eating")
      println("I'm still eating")
    end eat
  end BracelessAnimal2






  def main(args: Array[String]): Unit = {

  }


}
