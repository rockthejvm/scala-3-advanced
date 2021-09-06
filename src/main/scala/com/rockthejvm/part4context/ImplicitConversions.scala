package com.rockthejvm.part4context

// special import
import scala.language.implicitConversions

object ImplicitConversions {

  case class Person(name: String) {
    def greet(): String = s"Hi, I'm $name, how are you?"
  }

  val daniel = Person("Daniel")
  val danielSaysHi = daniel.greet()

  // special conversion instance
  given string2Person: Conversion[String, Person] with
    override def apply(x: String) = Person(x)

  val danielSaysHi_v2 = "Daniel".greet() // Person("Daniel").greet(), automatically by the compiler

  def processPerson(person: Person): String =
    if (person.name.startsWith("J")) "OK"
    else "NOT OK"

  val isJaneOk = processPerson("Jane") // ok - compiler rewrites to processPerson(Person("Jane"))

  /*
    Use-cases for implicit conversions
      - auto-box types
      - use multiple (often unrelated) types with the same meaning in your code, interchangeably
   */

  def main(args: Array[String]): Unit = {

  }
}
