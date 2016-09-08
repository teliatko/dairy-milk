// -------
// Example
// -------
// source: http://debasishg.blogspot.ie/2013/02/modular-abstractions-in-scala-with.html

// Some basic model for currency
sealed trait Currency
case object USD extends Currency
case object EUR extends Currency

import java.util.Date
import java.util.Calendar

// Some ADT to represent Account
case class Account(no: String, name: String, openedOn: Date, status: String)

// First component
trait BalanceComponent {
  type Balance // works on abstract 'later defined' type

  def balance(amount: Double, currency: Currency, asOf: Date): Balance
  def inBaseCurrency(balance: Balance): Balance
}

// Second component, which is dependent from [[BalanceComponent]]
// dependency(ies) are expressed w/composition not inheritance like in self-type annotation style
trait Portfolio {
  val balanceComp: BalanceComponent // dependency, should be defined later
  import balanceComp._ // for convenience, not need to use '.' notation when uding types

  def currentPortfolio(account: Account): List[Balance]
  // def currentPortfolio(account: Account): List[balanceComp.Balance] // alternative notation
}

// Specialization of [[BalanceComponent]]
trait SimpleBalanceComponent extends BalanceComponent {
  type Balance = (Double, Currency, Date) // defines type as tuple of 3

  // Some abstract members needed for 'inBaseCurrency' method
  // ... in reality most probably provided by another dependency
  def baseFactor: Double
  def baseCurrency: Currency

  // implementations of methods defined in [[BalanceComponent]]
  override def balance(amount: Double, currency: Currency, asOf: Date) =
    (amount, currency, asOf)
  override def inBaseCurrency(balance: Balance) =
    ((balance._1) * baseFactor, baseCurrency, balance._3)
}

// Another specialization of [[BalanceComponent]]
trait CustomBalanceComponent extends BalanceComponent {
  type Balance = BalanceRep // defines type via case class, i.e. ADT

  case class BalanceRep(amount: Double, currency: Currency, asOf: Date)

  def baseFactor: Double
  def baseCurrency: Currency

  override def balance(amount: Double, currency: Currency, asOf: Date) =
    BalanceRep(amount, currency, asOf)
  override def inBaseCurrency(balance: Balance) =
    BalanceRep(balance.amount * baseFactor, baseCurrency, balance.asOf)
}

trait ClientPortfolio extends Portfolio {
  import balanceComp._ // for convenience, not need to use '.' notation when using types

  override def currentPortfolio(account: Account): List[Balance] = {
    List(
      balance(1000, EUR, Calendar.getInstance.getTime),
      balance(1500, USD, Calendar.getInstance.getTime)
    )
  }
  // def currentPortfolio(account: Account): List[balanceComp.Balance] // alternative notation
  // def currentPortfolio(account: Account) // alternative notation, kept for compiler
}

// Decorator pattern defined in sense od components
trait Auditing extends Portfolio { // inheriting defines that [[Auditing]] will be [[Portfolio]]
  val semantics: Portfolio // inner semmantics, i.e. decorated [[Portfolio]]
  val balanceComp: semantics.balanceComp.type // reference to [[Portfolio]] dependency to [[BalanceComponent]]
  // lazy val balanceComp: semantics.balanceComp.type = semantics.balanceComp // it can be defined lazy, thus no need to redaclare it in sub classes

  import balanceComp._ // !!! IMPORTANT !!! w/o this it does not work, it simply import types from not yet known wrapped instance dependency

  override def currentPortfolio(account: Account) = { // inference kept for compiler, can by ecplicityl typed too as List[Balance]
    semantics.currentPortfolio(account) map inBaseCurrency
  }
}

// Instantiation of [[BalanceComponent]]s
object SimpleBalanceComponent extends SimpleBalanceComponent {
  override val baseFactor = 10.0
  override val baseCurrency = EUR
}
object CustomBalanceComponent extends CustomBalanceComponent {
  override val baseFactor = 10.0
  override val baseCurrency = EUR
}

// Instantiation of [[Portfolio]] components decorated w/[[Auditing]]
object ClientPortfolioAuditService extends Auditing {
  val semantics = new ClientPortfolio { val balanceComp = SimpleBalanceComponent }
  val balanceComp: semantics.balanceComp.type = semantics.balanceComp // not needed when 'balanceComp' defined as lazy in [[Auditing]]
}

object AnotherClientPortfolioAuditService extends Auditing {
  val semantics = new ClientPortfolio { val balanceComp = CustomBalanceComponent }
  val balanceComp: semantics.balanceComp.type = semantics.balanceComp // not needed when 'balanceComp' defined as lazy in [[Auditing]]
}

// Usage-side, all correctly binded and works
val firstPortfolio = ClientPortfolioAuditService.currentPortfolio(Account("1", "Acc. One", Calendar.getInstance.getTime, "active"))
val secondPortfolio = AnotherClientPortfolioAuditService.currentPortfolio(Account("2", "Acc. Two", Calendar.getInstance.getTime, "active"))
