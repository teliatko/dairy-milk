// ---------
// Example 1
// ---------
// source: http://dcsobral.blogspot.de/2010/06/implicit-tricks-type-class-pattern.html

// Type class in scala sense
abstract class Acceptable[T] {
  // It can require some methods to define by subtypes
  // def someOp
}

object Acceptable {
  // Known [[Acceptable]] type class specializations for known type
  // in this case Int and Long
  implicit object IntOk extends Acceptable[Int]
  implicit object LongOk extends Acceptable[Long]
}

// Client of acceptable, show ad-hoc polymorhism which can be achieved with type classes
object Client {
  // Function using [[Acceptable]]
  // Note: definition as 'T: Acceptable' is called context bound
  def accept[T: Acceptable](t: T) = t

  // Another function using [[Acceptable]]
  // Note: in full syntax you can introduce another implicit after evidence parameter
  def fullAccept[T](t: T)(implicit ev: Acceptable[T], num: Numeric[T]) = t
}

// ---------
// Example 2
// ---------
// source: http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html

object Maths {
  // Type class definition
  import annotation.implicitNotFound // allows customization of compiler errors
  @implicitNotFound("No member of type class NumberLike in scope for ${T}")
  trait NumberLike[T] {
    def plus(x: T, y: T): T // prescribes required operations
    def minus(x: T, y: T): T // mostly stateless, operations are pure functions
    def divide(x: T, y: Int): T
  }

  object NumberLike {
    // Type class instantiation or specialization
    implicit object NumberLikeInt extends NumberLike[Int] {
      def plus(x: Int, y: Int): Int = x + y
      def minus(x: Int, y: Int): Int = x - y
      def divide(x: Int, y: Int): Int = x / y
    }
  }
}

object Stats {
  import Maths.NumberLike

  // Usage-side of type class with full syntax
  def mean[T](xs: Vector[T])(implicit num: NumberLike[T]): T = {
    num.divide(xs.reduce(num.plus(_, _)), xs.size)
    // was: xs.reduce( _ + _ ) / xs.size
  }
  // Usage-side of type class via context bound, just syntactic sugar
  // Note: possible only when one type-class is used and it requires only one type parameter
  def median[T: NumberLike](xs: Vector[T]): T = xs(xs.size / 2)
  // Context bound again
  def quartiles[T: NumberLike](xs: Vector[T]): (T, T, T) =
    ( xs(xs.size / 4), median(xs), xs(xs.size / 4 * 3) )
  // Using context bound and retrieveing evidence
  def iqr[T: NumberLike](xs: Vector[T]): T = quartiles(xs) match {
    case (lowerQ, _, upperQ) =>
      implicitly[NumberLike[T]].minus(upperQ, lowerQ) // uses Predef.implicitly
  }
}
