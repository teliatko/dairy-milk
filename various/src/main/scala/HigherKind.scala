// ------------------------
// Example w/o higher kinds
// ------------------------

// Iterable definition
trait Iterable_1[T] {
  def filter(p: T => Boolean): Iterable_1[T]
  def remove(p: T => Boolean): Iterable_1[T] = filter(x => !p(x))
}

// Iterable application
trait List_1[T] extends Iterable_1[T] {
  def filter(p: T => Boolean): List_1[T] // !!! need to duplicate definitions due to return type
  override def remove(p: T => Boolean): List_1[T] = filter(x => !p(x)) // !!! even need to duplicate whole implementation
}

// Proper type
class ListOfInts_1 extends List_1[Int] {
  def filter(p: Int => Boolean): ListOfInts_1 = new ListOfInts_1
}


// ---------------------------------------
// Example w/o higher kinds with "my type"
// ---------------------------------------

// Iterable definition
trait Iterable_2[T] {
  def filter(p: T => Boolean): this.type
  def remove(p: T => Boolean): this.type = filter(x => !p(x))
}

// Iterable application
trait List_2[T] extends Iterable_1[T] // !!! no duplication of definitions either implementation

// Proper type
class ListOfInts_2 extends List_2[Int] {
  def filter(p: Int => Boolean): ListOfInts_2 = new ListOfInts_2 // !!! implementation via inheritance
}


// -----------------------------------------------------------
// Example w/ higher kinds with type constructor polymorphism
// -----------------------------------------------------------

// Her majesty, higher kind :)
import scala.language.higherKinds

// Iterable definition
trait Iterable_3[T, Container[X]] {
  def filter(p: T => Boolean): Container[T]
  def remove(p: T => Boolean): Container[T] = filter(x => !p(x))
}

// Iterable application (just and example)
trait List_3[T] extends Iterable_3[T, List_3]


// -----------------------------------------------------------
// Full-blown implementation via type constructor polymorphism
// -----------------------------------------------------------

trait Builder[Container[X], T] {
  def +=(el: T): Unit
  def finalise(): Container[T]
}

trait Iterator[T] {
  def next(): T
  def hasNext: Boolean

  def foreach(op: T => Unit): Unit =
    while(hasNext) { op(next()) }
}

trait Buildable[Container[X]] { // type constructor parameter, while it's a primary purpose to build a container
  def build[T]: Builder[Container, T]
  def buildWith[T](f: Builder[Container, T] => Unit): Container[T] = {
    val buff = build[T]
    f(buff)
    buff.finalise()
  }
}

trait Iterable_4[T] {
  type Container[X] <: Iterable_4[X] // type constructor member, while most of the time clients are not concerned with exact type of container

  def elements: Iterator[T]

  def mapTo[U, C[X]](f: T => U)(b: Buildable[C]): C[U] = {
    val buff = b.build[U]
    val elems = elements

    while(elems.hasNext) {
      buff += f(elems.next)
    }
    buff.finalise()
  }

  def flatMapTo[U, C[X]](f: T => Iterable_4[U])(b: Buildable[C]): C[U] = {
    val buff = b.build[U]
    val elems = elements

    while(elems.hasNext) {
      f(elems.next).elements.foreach { el =>
        buff += el
      }
    }
    buff.finalise()
  }

  def filterTo[C[X]](p: T => Boolean)(b: Buildable[C]): C[T] = {
    val elems = elements

    b.buildWith[T] { buff =>
      while(elems.hasNext) {
        val el = elems.next
        if(p(el)) buff += el
      }
    }
  }

  def map[U](f: T => U)(b: Buildable[Container]): Container[U] =
    mapTo[U, Container](f)(b)
  def flatMap[U](f: T => Container[U])(b: Buildable[Container]): Container[U] =
    flatMapTo[U, Container](f)(b)
  def filter(p: T => Boolean)(b: Buildable[Container]): Container[T] =
    filterTo[Container](p)(b)
  def remove(p: T => Boolean)(b: Buildable[Container]): Container[T] =
    filter(x => !p(x))(b)
}

trait List_4[T] extends Iterable_4[T]

object ListBuildable extends Buildable[List_4] {
  import scala.collection.mutable.ListBuffer

  def build[T]: Builder[List_4, T] = new ListBuffer[T] with Builder[List_4, T] {
    def finalise(): List_4[T] = toList
  }
}
