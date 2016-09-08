class A { 
  val x = { println("A.x"); List(1) }
  println(x.head) 
}

class B extends A {
  override val x = { println("B.x"); List(2) }
}
