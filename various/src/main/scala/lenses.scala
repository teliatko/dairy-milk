import scalaz._
import Scalaz._

case class Price(amount: Option[Double])
case class Discount(amount: Option[Double])
case class LineItem(price: Price, discount: Option[Discount])

val li1 = LineItem(Price(Some(19.99)), None)
val li2 = LineItem(Price(None), Some(Discount(Some(7.99))))

val price: Lens[LineItem, Price] = Lens( _.price, (li, p) => li.copy( price = p ) )
val priceAmount: Lens[Price, Option[Double]] = Lens( _.amount, (p, a) => p.copy( amount = a ) )
val discount: Lens[LineItem, Option[Discount]] = Lens( _.discount, (li, d) => li.copy( discount = d ) )
val discountAmount: Lens[Discount, Option[Double]] = Lens( _.amount, (d, a) => d.copy( amount = a ) )

def und[A, B, C](l1: Lens[A, Option[B]], l2: Lens[B, Option[C]]) = new Lens[A, Option[C]](
    (a) => l1.get(a).flatMap(b => l2.get(b))
  , (a, c) => {
  	  l1.get(a) match {
  	    case Some(b) => l1.set(a, Some(l2.set(b, c)))
  	    case None => a
  	  }
  	}
) with PartialLens[A, C]

def partial[A, B](l: Lens[A, Option[B]]): Lens[A, Option[B]] with PartialLens[A, B] = {
	new Lens[A, Option[B]](l.get, l.set) with PartialLens[A, B]
}

trait PartialLens[A, B] { req: Lens[A, Option[B]] =>
  val underlying: Lens[A, Option[B]] = req
  def set(a: A, b: B): A = req.set(a, Option(b))
}

val lineItemPriceAmount = price andThen priceAmount
val lineItemDiscountAmount = und(discount, discountAmount)

case class ManualDiscount(tpe: String, percent: Option[Double], amount: Option[Double], amountIncludesTax: Option[Boolean])
val tpe: Lens[ManualDiscount, String] = Lens( (md) => md.tpe, (md, t) => md.copy( tpe = t ))
val percent: PartialLens[ManualDiscount, Double] = PartialLens( _.percent, (md, p) => md.copy( percent = p ) )
val amount: PartialLens[ManualDiscount, Double] = PartialLens( _.amount, (md, a) => md.copy( amount = a ) )
val amountIncludesTax: PartialLens[ManualDiscount, Boolean] = PartialLens( _.amountIncludesTax, (md, ait) => md.copy( amountIncludesTax = ait ) )
val mdp = ManualDiscount(tpe = "primary", percent = Some(0.11), amount = None, amountIncludesTax = None)
val mda = ManualDiscount(tpe = "primary", amount = Some(10), amountIncludesTax = Some(true), percent = None)

def combine[A,B,C](l1: Lens[A,B], l2: Lens[A,C]): Lens[A,(B,C)] = Lens[A,(B,C)](
  (a) => (l1.get(a), l2.get(a)),
  (a, bc) => l1.set( l2.set(a, bc._2), bc._1) 
)

def combinePartial[A,B,C](l1: Lens[A, Option[B]], l2: Lens[A, Option[C]]): Lens[A, Option[(B, C)]] with PartialLens[A,(B,C)] = {
  partial(
  	combine(l1, l2).xmap( 
  	  _ match {
  	 	case (Some(b), Some(c)) => Some((b, c))
 	  	case _ => None
  	  }
  	)(
  	  _ match {
  	  	case Some((b, c)) => (Some(b), Some(c))
  	  	case _ => (None, None)
  	  }
    )
  )
}