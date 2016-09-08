// --------------------
// Higher Kinded Types
// --------------------
//
// - ability to generically abstract across 'things' that take type parameters
// - it allows us write a library that works with much wider array of classes without feature duplication
//
// = Type Constructors =
// e.g. List[_] is not a proper (fully classified) type, in some extend it's considerred type constructor
// while it can create another fully classified types like List[Int], List[String] etc.
//
// When we have List[_] as type constructor, we can think about having different kinds of type which it can produce,
// classified by how many parameters it takes. In this case one, we say it has kind:
//
// (* -> *)
//
// This says: given one type produce another. For instance given String produce List[String].
//
// Something that takes 2 type paremeters, like Map[_, _] or Function1[_, _] has kind:
//
// (* -> * -> *)
//
// This says: given one type and then another produce final type. For instance given the key type Int
// and the value type String produce the final type Map[Int, String].
//
// Futhermore you can have types that not only take type, but something that can be futher parametrized
// and take type parameters itself, like Functor[F[_]]. It has kind:
//
// ((* -> *) -> *)
//
// This says: given higher kinded type produce final type. For instance given a type constructor List
// produce the final type Functor[List]
//
// Note:
// - correct type constructor in Scala of List is List[+T]
// - List[+T] is called generic or first-order type, while it abstracts only
//   over given type T directlty and this type cannot be parametrized next.
// - Imagine a type Foo that takes type M — Foo<M>. But, what if M is a type constructor itself? Then Foo is higher-kinded-type.
// - Use case as Functor, Monad - terms that come from abstract algebra and category theory

// -------
// Usage:
// -------

// Covariant fuunctor, defines operation which we can consistently apply
// on the structure of the same shape. I.e. it preserves the shape that
// having a box holding of type A and function A => B and we can get back
// box holding things of type B.
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

// implement for java's List
// note that the presence of mutation in the Java collections
// breaks the Functor laws
import java.util.{ List => JList }
implicit object JavaListFunctor extends Functor[JList] {
  import collection.JavaConverters._

  def map[A, B](fa: JList[A])(f: A => B): JList[B] =
    (for (a <- fa.asScala) yield f(a)).asJava
}

case class Box2[A](a1: A, a2: A)
implicit object Box2Functor extends Functor[Box2] {
  def map[A, B](b: Box2[A])(f: A => B): Box2[B] =
    Box2(f(b.a1), f(b.a2))
}

// and use it (via context bound)
def describe[A, F[_]: Functor](fa: F[A]) =
  implicitly[Functor[F]].map(fa)(a => a.toString)

// alternate syntax (with explicit implict block)
def describe2[A, F[_]](fa: F[A])(implicit functor: Functor[F]) =
  functor.map(fa) { _.toString }

case class Holder(i: Int)
val jlist: JList[Holder] = {
  val l = new java.util.ArrayList[Holder]()
  l add Holder(1); l add Holder(2); l add Holder(3)
  l
}

val list = describe(jlist) // list: java.util.List[String] = [Holder(1), Holder(2), Holder(3)]

val box2 = describe(Box2(Holder(4), Holder(5)) // box: Box2[String] = Box2(Holder(4),Holder(5))

// So, we have a described function that works for any type that we can map over


// ----------------------------------------
// Usage w/traditional sub-typing approach
// ----------------------------------------

/**
 * note we need a recursive definition of F as a subtype of Functor
 * because we need to refer to it in the return type of map(…)
 */
trait Functor[A, F[_] <: Functor[_, F]] {
  def map[B](f: A => B): F[B]
}

case class Box[A](a: A) extends Functor[A, Box] {
  def map[B](f: A => B) =
    Box(f(a))
}

def describe[A, F[A] <: Functor[A, F]](fa: F[A]) =
  fa.map(a => a.toString)

val box = describe(Box(Holder(6))) // box: Box[String] = Box(Holder(6))

// Example quite nicely shows how subtype polymorphism is strictly less powerful and also more complicated
// (both syntactically and semantically) than ad-hoc polymorphism via type-classes.

// HKT in nutshell:
// - libraries that heavily rely on HKT: scalaz, spire, cats, shapeless
// - important for creating type-classes
// - REPL has :kind command

// Source:
// http://blogs.atlassian.com/2013/09/scala-types-of-a-higher-kind/

// ------------
// HKT in REPL
// ------------

scala> :kind Option
scala.Option's kind is F[+A]

scala> :k -v Either
scala.util.Either's kind is F[+A1,+A2]
* -(+)-> * -(+)-> *
This is a type constructor: a 1st-order-kinded type.

scala> :k Int
scala.Int's kind is A

scala> class Foo
defined class Foo

scala> new Foo { def empty = true }
res0: Foo{def empty: Boolean} = $anon$1@4ddced80

scala> :k -v res0
Foo{def empty: Boolean}'s kind is A
*
This is a proper type.

scala> :k -v scala.collection.generic.Sorted
scala.collection.generic.Sorted's kind is F[K,+This <: scala.collection.generic.Sorted[K,This]]
* -> *(scala.collection.generic.Sorted[K,This]) -(+)-> *
This is a type constructor: a 1st-order-kinded type.

// Alternate eta-expansion syntax in REPL
scala> import scala.reflect.runtime.universe._

scala> typeOf[Either[Any, Any]].typeConstructor.etaExpand
res10: $r.intp.global.Type = [+A, +B]Either[A,B]

scala> trait Monad[F[_]]
warning: there were 1 feature warning(s); re-run with -feature for details
defined trait Monad

scala> typeOf[Monad[Any]].typeConstructor.etaExpand
res11: $r.intp.global.Type = [F[_]]Monad[F]

// !!!!! INVESTIGATE !!!!!
// - why error: not found: value Functor, is it just REPL problem or what?
// - what is the purpose of value scala.language.higherKinds and why should I import it?
// - how does the Scala 2.12.x behave in this respect? Is there any warning too?
// - What is eta-expansion in reflect utils?
// - How the :type or :t works in REPL?
// - Find-out if there are any issues in Scala/REPL related to kinds
// * How to make REPL colourful?

scala> trait Functor[F[_]] {
     | def map[A, B](fa: F[A])(f: A => B): F[B]
     | }
<console>:10: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scala docs for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
       trait Functor[F[_]] {
                     ^
defined trait Functor

scala> :k Functor
<console>:11: error: not found: value Functor
       Functor
       ^
