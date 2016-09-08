// ----------------------------
// Tips and Tricks, taken from:
// https://speakerdeck.com/agemooij/between-zero-and-hero-scala-tips-and-tricks-for-the-intermediate-scala-developer
// ----------------------------


// -------------------------------------------------
// Type classes 'aka ad-hoc polymorphism
// -------------------------------------------------

// Type-class interface abstacting over one type T
trait OneType[T] {
  def outsourcedOp(t: T): T
}

// Type-class interface abstracting over two types T1 and T2
// Note: type-class definition can have as many types as is needed
// difference is in possibility to write context-bounds
@annotation.implicitNotFound("No member of type class TwoTypes not foud for combination of types ${T1} and ${T2}.") // best practice, allows to define explanation for user
trait TwoTypes[T1, T2] {
  def outsourceOp(t: T1): T2 = ??? // impl. not important
}

// Definition of common type classes
object OneType {
  // Definition of OneType type-class at place, directly as implicit
  implicit def stringOneType =
    new OneType[String] {
      def outsourceOp(t: String) = t * 2
    }

  // Definition of OneType type-class as reusable class (or trait)
  class IntOneType extends OneType[Int] { // This has it's own benefits when need more than one instance per type T
    def outsourceOp(n: Int) = n * 2
  }
  // Definition of impicit object
  implicit object intOneType extends IntOneType
}

// Shows client interface using type-classes
object Client {
  // Defined via implicits directly
  def firstAttempt[T](t: T)(implicit oneType: OneType[T]) = ???

  // Defined via context-bounds
  // Note: possible only when one type-class is used and it requires only one type parameter
  def secondAttempt[T : OneType](t: T) = {
    val oneType = implicitly[OneType[T]] // type class instance shoud be aquired explicitly
    ???
  }
}

// Open:
// - Look on constraint of context-bounds, more pariculary "possible only when one type-class is used and it requires only one type parameter"?
// - How to use type-constructors with type-classes?
// - Check-out possible options to encode type-classes? See https://github.com/mpilquist/simulacrum for details
