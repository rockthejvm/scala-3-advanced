package com.rockthejvm.part1advancedscala

import java.util.NoSuchElementException

object AdvancedPatternMatching {

  // objects
  // constants
  // vars
  // infix patterns
  // lists
  // case classes

  val daniel = new Person("Daniel", 102)

  class Person(val name: String, val age: Int) {

    object Person {
      def unapply(person: Person): Option[(String, Int)] =
        if (person.age < 21) None
        else Some((person.name, person.age))

      def unapply(age: Int): Option[String] =
        if (age < 21) Some("Minor")
        else Some("Legally allowed to drink")
    }



    val danielPM = daniel match {
      case Person(n, a) => s"Hi there I'm $n"
    }

    val danielsLegalStatus = daniel.age match {
      case Person(status) => s"Daniels legal drinking status is $status"
    }

    // boolean patterns
    object even {
      def unapply(arg: Int): Boolean = arg % 2 == 0
    }

    object singleDigit {
      def unapply(arg: Int): Boolean = arg > -10 && arg < 10
    }

    val n: Int = 43
    val mathProperty = n match {
      case even() => "even num"
      case singleDigit() => "one digit num"
      case _ => "nothing special"
    }
  }

  // infix patterns
  infix case class Or[A, B](a: A, b: B)
  val anEither = Or(2, "two")
  val humanDesctiptionEither = anEither match {
    case number Or string => s"$number is written as $string"
  }

    val aList = List(1,2,3)
    val listPM = aList match {
      case 1 :: rest => "a list starting with 1"
      case _ => "something not interesting"
    }

    // vararg pattern
    val vararg = aList match {
      case List(1, _*) => "starts with one"
      case _ => "something else"
    }

    abstract class MyList[A] {
      def head: A = throw new NoSuchElementException
      def tail: MyList[A] = throw new NoSuchElementException
    }

    case class Empty[A]() extends MyList[A]
    case class Cons[A](override val head: A, override val tail: MyList[A]) extends MyList[A]


  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
      if (list == Empty()) Some(Seq.empty)
      else unapplySeq(list.tail).map(restOfSequence => list.head +: restOfSequence)
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty())))
  val varargCustom = myList match {
    case MyList(1, _*) => "List starting with 1"
    case _ => "not important"
  }

  // custom return type for unapplu
  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = person.name
    }
  }

  val weirdPerson = daniel match {
    case PersonWrapper(name) => s"hey my name is $name"
  }


  def main(args: Array[String]): Unit = {

  }
}
