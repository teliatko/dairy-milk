def uber[E, C[_]](code: => C[E]): Unit = {
  println(s"called Seq variant")
}

def uber[E](code: => E): Unit = {
  println(s"called fallback variant")
}

type Discriminator = () => Seq[_]

def run[T](code: => T)(implicit c: Not[(T =:= (() => Seq[_]))]) = "for others"
def run[T](code: => T)(implicit c: (T <:< (() => Seq[_]))) = "for Seq"

val seqCode = () => Seq(1, 2, 3)
val otherCode = () => "I'm other one"
