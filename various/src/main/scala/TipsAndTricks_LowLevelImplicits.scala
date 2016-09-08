// ----------------------------
// Tips and Tricks, taken from:
// https://gist.github.com/retronym/228673
// ----------------------------


// ----------------------------------------------------
// Low level implicits in combination with type-classes
// ----------------------------------------------------

// Let's have a model
case class Foo(baz: String)
case class Bar(baz: String)

// And some Reader
class Reader[A] {
  def read(s: String): A = ???
}
// Companion object contains conficting implicits
object Reader {
  implicit val fooReader = new Reader[Foo] // This is ambiguous
  implicit val barReader = new Reader[Bar] // it can be solved with moving one to trait from which this object extends
}

def convert[A](s: String)(implicit reads: Reads[A]): A = reads.read(s)
// def f(s: String): Foo = convert(s) // do not compile

// Let's have a model
object Low {
  def low = "object Low"
  def shoot = "missed!"
}

object High {
  def high = "object High"
  def shoot = "bulls eye!"
}

// Low priority implicits
trait LowPriority {
  implicit def intToLow(a: Int): Low.type = Low
}
// High priority overrides low priority ones
object HighPriority extends LowPriority {
  implicit def intToHigh(a: Int): High.type = High
}

import HighPriority._

val a: Int = 1
print((a.low, a.high, a.shoot)) // (object Low,object High, bulls eye!)
