// ----------------------------
// Tips and Tricks, taken from:
// https://speakerdeck.com/agemooij/between-zero-and-hero-scala-tips-and-tricks-for-the-intermediate-scala-developer
// ----------------------------


// -------------------------------------------------
// Using NoStackTrace trait when defining exceptions
// -------------------------------------------------

// Known/controled exceptions can be mixed with NoStackTrace to gain performance
object ShowCase {

  import scala.util.control.NoStackTrace

  // Model of result for our example
  sealed trait Result
  case class Correct(n: Int) extends Result
  case class Wrong(msg: String) extends Result

  // Custom application exception
  // - we are sure that we can omit stack trace while we control the construction of these
  case class ApplicationException(msg: String) extends RuntimeException(msg) with NoStackTrace

  // Converting some result exception
  // - controlled fashion where the exception happened
  // - we suppose that 'msg' already contains all the error information necessary
  // - thus we can omit the stack trace
  def processResult(thunk: => Result): Unit =
    thunk match {
      case Correct(n) => println(s"Result was: $n")
      case Wrong(msg) => throw ApplicationException(msg)
    }

}

// Client usage:
// import ShowCase._
// val wrong = Wrong("This is wrong")
// ShowCase.processResult(wrong)
// val ok = Correct(0)
// ShowCase.processResult(ok)
