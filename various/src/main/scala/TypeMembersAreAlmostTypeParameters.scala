// -----------------------------------------
// Type Members Are (Almost) Type Parameters
// -----------------------------------------
// source: http://typelevel.org/blog/2015/07/13/type-members-parameters.html

// Class definition using 'type member'
class Foo {
  type Bar // TODO: don't understand why this compile, type is not defined
}

// Class definition using 'type parameter'
class Baz[Qux]

// Rule of thumb:
// 'type parameter' is usually more convenient and harder to screw up, but
// 'type member'    is better when need to refer to name (while type member is type alias too)
//                  and when used "existentially"
//
// another rule
// 'type parameter' is always visible from outside, it needs to be provided from outside
//                  even when you cannot directly refer to it
// 'type member'    is, as a name suggest, primarily internal property of class,
//                  hence that's why it's bounded to class as 'path dependent' in first place

// ------------------------
// Example: Functional List
// ------------------------
// P for 'type parameter' version
// M for 'type member' version

// List defined using 'type parameter'
sealed abstract class PList[T]
final case class PNil[T]() extends PList[T]
final case class PCons[T](head: T, tail: PList[T]) extends PList[T]

// List defined using 'type member'
sealed abstract class MList { self => // TODO: check-out what is this 'self' hack, is the self =:= this
  type T
  def uncons: Option[MCons { type T = self.T }] // TODO: wtf is uncons here
}
sealed abstract class MNil extends MList {
  def uncons = None
}
sealed abstract class MCons extends MList { self =>
  val head: T
  val tail: MList //{ type T = self.T }
  def uncons = Some(self: MCons { type T = self.T })
}
// Construction of our 'type member' counter parts
object MNil {
  def apply[T0](): MNil {type T = T0} =
    new MNil {
      type T = T0
    }
}
object MCons {
  def apply[T0](hd: T0, tl: MList {type T = T0}): MCons {type T = T0} =
    new MCons {
      type T = T0
      val head = hd
      val tail = tl
    }
}

// It's obvious that 'type parameter' version is more straightforward
// as an implementation for functional list data structure:
// - it's more concise
// - it does provide instance creation by virtue of being case classe for each type of node,
//   but even as plain class with 'new' the construction is more straightforward
