// ----------------------------
// Tips and Tricks, taken from:
// https://speakerdeck.com/agemooij/between-zero-and-hero-scala-tips-and-tricks-for-the-intermediate-scala-developer
// ----------------------------


// ------------
// Type Aliases
// ------------

// How to define them
object Oops {
  type MyList[T] = List[T]
  val MyList = List // this is refrence to companion object

  def doSomething = MyList(1, 2, 3) // usage of companion object reference
}

// ----------------------
// Case 1 - API usability

// Wrong API, users need to import java.util.concurrent.TimeUnit
object Oops_Wrong {
  import java.util.concurrent.TimeUnit

  def elapsed(start: Long, unit: TimeUnit) = {
    System.nanoTime() - unit.toNanos(start)
  }
}

// Client usage:
// import java.util.concurrent.TimeUnit
// Oops_Wrong.elapsed(System.currentTimeMillis, TimeUnit.MILLISECONDS)

// Better API, users just need to import your object
// - use only when Oops_Better forms natural boundary between your modules/components
// - i.e. Oops_Better is self containing
object Oops_Better {

  // Java example
  import java.util.concurrent.TimeUnit

  type Unit = TimeUnit // aliasing the type
  object Unit { // aliasing the enumeration values, only because of Java
    val Seconds = TimeUnit.SECONDS
    val Milliseconds = TimeUnit.MILLISECONDS
    val Nanoseconds = TimeUnit.NANOSECONDS
  }

  // API method
  def elapsed(start: Long, unit: Unit) = {
    System.nanoTime() - unit.toNanos(start)
  }

  // Scala example
  import scala.math.{BigDecimal => BD} // import to avoid disambiguation

  type RoundingMode = BD.RoundingMode.RoundingMode // aliasing the type
  val RoundingMode = BD.RoundingMode // aliasing the in object with enumeration values

  val Up = BD.RoundingMode.UP // when only some of the values needs to be supported
  val Ceil = BD.RoundingMode.CEIL // can be wrapped to object, or package see later

  // API method
  def setScale(scale: Int, mode: RoundingMode) =
    "just an example"
}

// Client usage:
// import Oops_Better._
// Oops_Better.elapsed(System.currentTimeMillis, Unit.MILLISECONDS) // uses complete Oops_Better call
// elapsed(System.currentTimeMillis, Unit.MILLISECONDS) // uses complete constructor invocation

// Best fit is to use 'package object' for type aliases
package oops { // 'package' does not work in REPL, use object instead
  import scala.math.{BigDecimal => BD} // import to avoid disambiguation
  type RoundingMode = BD.RoundingMode.RoundingMode // aliasing the type
  val RoundingMode = BD.RoundingMode // aliasing the in object with enumeration values

  object Oops_Packaged {
    // API method
    def setScale(scale: Int, mode: RoundingMode) =
      "just an example"
  }
}

// Client usage:
// import oops._
// Oops_Packaged.setScale(1, RoundingMode.UP)


// --------------------------------
// Case 2 - use case simplification

// Sometimes the type signatures get complicated

object Oops_Complicated {
  def doSomething[T](fn: Int => Future[Either[String, T]]) = ???
}

// A few type aliases make an API clean

object Oops_Cleaner {
  type ErrorHandling[T] = Either[String, T]
  type AsyncComputation[T] = Future[ErrorHandling[T]]

  def doSomething[T](fn: Int => AsyncComputation[T]) = ???
}
