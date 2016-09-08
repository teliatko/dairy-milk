// ----------
// Example 1: NotNothing
// ----------
// source: http://hacking-scala.org/post/73854628325/advanced-type-constraints-with-type-classes

// Problem, resolution of type-class =:= works for Nothing too
def foo[T](implicit a: T =:= T) = a
foo[Nothing]
// in combination with TypeTags, it can infer Nothing even when type is provided
val foo1 = foo
val foo2: String = foo

// !!! BEWARE !!! - not problem anymore in 2.11.6
// provided solution:
// - forces user to provide type on call-side
// - gives user explicit error from NotNothing type-class
import scala.reflect.runtime.universe.TypeTag

@annotation.implicitNotFound("Sorry, unable to figure out the type. Please provide it explicitly.")
trait NotNothing[T]

object NotNothing {
  private val evidence: NotNothing[Any] =
    new Object with NotNothing[Any]

  implicit def notNothingEvidence[T](implicit n: T =:= T): NotNothing[T] =
    evidence.asInstanceOf[NotNothing[T]]
}

def inject1[T](implicit tt: TypeTag[T], nn: NotNothing[T]) = tt.toString

// inject1 // does not compile
// inject1[Nothing] // does not compile
// val foo: String = inject1 // does not compile
inject1[String] // compiles
inject1[Int] // compiles


// ----------
// Example 2: "Not" type-class 'aka non-/existence check
// ----------

sealed trait Existence
trait Exists extends Existence
trait NotExists extends Existence

trait IsTypeClassExists[TypeClass, Answer]

object IsTypeClassExists {
  private val evidence: IsTypeClassExists[Any, Any] =
    new Object with IsTypeClassExists[Any, Any]

  implicit def typeClassExistsEv[TypeClass, Answer](implicit a: TypeClass) =
    evidence.asInstanceOf[IsTypeClassExists[TypeClass, Exists]]
}

@annotation.implicitNotFound("Argument does not satisfy constraints: Not ${T}")
trait Not[T]

object Not {
  private val evidence: Not[Any] = new Object with Not[Any]

  implicit def notEv[T, Answer](implicit a: IsTypeClassExists[T, Answer], ne: Answer =:= NotExists) =
    evidence.asInstanceOf[Not[T]]
}

def inject2[T](implicit tt: TypeTag[T], nn: Not[T =:= Nothing]) =
  tt.toString

// inject2 // does not compile
// inject2[Nothing] // does not compile
// val foo: String = inject2 // does not compile
inject2[String] // compiles
inject2[Int] // compiles


// ----------
// Example 3: "And" and "Or" type-classe
// ----------

@annotation.implicitNotFound("Argument does not satisfy constraints: ${A} And ${B}")
trait And[A, B]

object And {
  private val evidence: And[Any, Any] = new Object with And[Any, Any]

  implicit def bothExistEv[A, B](implicit a: A, b: B) =
    evidence.asInstanceOf[And[A, B]]
}

@annotation.implicitNotFound("Argument does not satisfy constraints: ${A} Or ${B}")
trait Or[A, B]

object Or {
  private val evidence: Or[Any, Any] = new Object with Or[Any, Any]

  implicit def aExistsEv[A, B](implicit a: A) =
    evidence.asInstanceOf[Or[A, B]]

  implicit def bExistsEv[A, B](implicit b: B) =
    evidence.asInstanceOf[Or[A, B]]
}

def union1[T](t: T)(implicit c: (T =:= String) Or (T =:= Int)) = t match {
  case s: String => println(s"Some nice string: $s")
  case i: Int => println(s"Some int: $i")
}

type V[A, B] = {type l[T] = (T <:< A) Or (T <:< B)}

def union2[T: (String V Int)#l](t: T) = t match {
  case s: String => println(s"Some nice string: $s")
  case i: Int => println(s"Some int: $i")
}

def sort[T: Ordering](list: List[T])(implicit c: Not[(T =:= String) Or Numeric[T]]) =
  list.sorted
