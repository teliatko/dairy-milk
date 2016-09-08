// ----------------------------
// Tips and Tricks, taken from:
// https://speakerdeck.com/agemooij/between-zero-and-hero-scala-tips-and-tricks-for-the-intermediate-scala-developer
// ----------------------------


// -----------------------------
// Auto-lifted Partial Functions
// -----------------------------

// High-order function paramaters can be written in shorter syntax
object ShowCase {

  // Input data
  val l = List(Some(1), None, Some(2), Some(3))

  // The map over list can be written
  l.map { el =>
    el match {
      case Some(i) => i % 2
      case None => 0
    }
  }

  // But Scala allows you to make it nicer
  l.map { // no 'match' here
    case Some(i) => i % 2
    case None => 0 // it is necessary to define exhausive list of cases
  }

  // Some collection combinators allow PartialFunctions directly in signature
  l.collect {
    case Some(i) => i % 2 // those doesn't require exhausive list of cases and can be used as map with filter directly
  }
}

// Open:
// - learn collection API in depth
