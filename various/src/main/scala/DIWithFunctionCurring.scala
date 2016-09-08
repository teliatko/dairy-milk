// -------
// Example
// -------
// source: http://debasishg.blogspot.in/2010/02/dependency-injection-as-function.html

// 2 services
trait CreditService
trait AuthService

// Payment depends on these services
case class RealPayment(
  creditSrevice: CreditService,
  authService: AuthService,
  amount: Int
)

// Some implementations of these services
case class PayPal(provider: String) extends CreditService
case class DefaultAuth(provider: String) extends AuthService

// Partial function of real payment factory with pay-pal
val paypalPayment = RealPayment(PayPal("bar"), _: AuthService, _: Int)
// Injecting default authentication again via partial function
paypalPayment(DefaultAuth("foo"), _: Int)

// Better alternative => curring
val realPaymentCurried = Function.curried(RealPayment.apply) // Factory of RealPayment is curried

// it can be used to gradually bind dependencies
val paypalPayment = realPaymentCurried(PayPal("bar"))
// or build the factory with all dependencies, i.e. module
val paypalPaymentWithDefaultAuth = paypalPayment(PayPal("bar"))(DefaulAuth("foo"))
