case class StateChange[S, A](transition: S => (S, A)) {
  
  def mapResult[B](fn: A => B): StateChange[S, B] = StateChange { state =>
  	val (newState, result) = transition(state)
  	(newState, fn(result))
  }

  def doItWithNewState[B](fn: A => StateChange[S, B]): StateChange[S, B] = StateChange { state =>
 	val (newState, result) = transition(state)
 	fn(result).transition(newState)
  }
}

case class Better(total: List[Int]) 

object Better {

  def sum(i: Int)(b: Better): (Better, Int) = {
  	val result = if (b.total.size > 1) 99 else b.total.sum
  	val newB = b.copy( total = i :: b.total)
  	(newB, result)
  }

}

