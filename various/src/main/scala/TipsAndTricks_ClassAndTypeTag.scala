// ----------------------------
// Tips and Tricks, taken from:
// https://speakerdeck.com/agemooij/between-zero-and-hero-scala-tips-and-tricks-for-the-intermediate-scala-developer
// ----------------------------


// --------------------
// Class Tag & Type Tag
// --------------------

// Easy accesss to runtime class and type information
object Oops {
  def header[T <: HttpHeader : ClassTag]: Option[T] = {
    val erasure = classTag[T].runtimeClass
    @annotation.tailrec def next(headers: List[HttpHeader]): Option[T] =
      if (headers.isEmpty) None
      else if (erasure.isInstance(headers.head)) Some(headers.head.asInstanceOf[T]) else next(headers.tail)
    next(headers)
  }
}

// Open:
// - look for appropriate simple example
// - what is the difference between ClassTag, TypeTag and Manifest ?
// - Is there yet something different like *Tag ?
// - Why presentation states that it is w/o 'runtime performance overhead'
// - Maybe write small tutorial on Scala reflection
