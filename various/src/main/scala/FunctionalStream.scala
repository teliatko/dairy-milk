package fn.data

trait Stream[+A] {
  import Stream._

  def uncons: Option[(A, Stream[A])]

  def foldRight[B](z: => B)(f: (A, => B) => B): B = uncons match {
    case None => z
    case Some((head, tail)) => f(head, tail.foldRight(z)(f))
  }

  def map[B](f: A => B): Stream[B] = {
    foldRight(empty[B]) { (head, tail) =>
      cons(f(head), tail)
    }
  }

  def filter(p: A => Boolean): Stream[A] = {
    foldRight(empty[A]) { (head, tail) =>
      if (p(head)) cons(head, tail)
      else tail
    }
  }

}

object Stream {
  def empty[A]: Stream[A] = new Stream[A] {
    def uncons = None
  }
  def cons[A](head: => A, tail: => Stream[A]): Stream[A] = new Stream[A] {
    def uncons = Some((head, tail))
  }
  def apply[A](as: A*): Stream[A] = {
    if (as.isEmpty) empty[A]
    else cons(as.head, apply(as.tail: _*))
  }
}
