object ExampleOfFnSyntax {

  // fully annotated definition
  val len1: String => Int = s => s.lenght

  // syntactic sugar in fn body
  val len2: String => Int = _.length

  // inference on declaration side
  val len3 = (s: String) => s.length

  // via object
  object len4 {
    def apply(s: String): Int = s.length
  }

}
