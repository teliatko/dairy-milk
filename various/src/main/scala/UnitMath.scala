package n4.graph.taxrules

import scala.util.Try

abstract sealed class BaseUnit(symbol: String) {
  override def toString(): String = symbol
}

object Meter extends BaseUnit("m")
object Kilogram extends BaseUnit("kg")
object MemoryBytes extends BaseUnit("mB")
object StorageBytes extends BaseUnit("sB")
object Cost extends BaseUnit("EUR")

case class PoweredUnit(unit: BaseUnit, power: Int) {

  def + (that: PoweredUnit): PoweredUnit = {
    checkIfSame(that)
    this
  }
  def - (that: PoweredUnit): PoweredUnit = {
    checkIfSame(that)
    this
  }
  def * (that: PoweredUnit): PoweredUnit = {
    checkIfSameUnit(that)
    this.copy( power = this.power + that.power)
  }
  def / (that: PoweredUnit): PoweredUnit = {
    checkIfSameUnit(that)
    this.copy( power = this.power - that.power)
  }

  private def checkIfSame(that: PoweredUnit) = require(that == this, s"Mixing apples with oranges ($this `vs $that)")
  private def checkIfSameUnit(that: PoweredUnit) = require(hasSameUnit(that), s"Mixing apples with oranges ($this `vs $that)")

  def hasSameUnit(that: PoweredUnit): Boolean = that.unit == this.unit

  override def toString(): String =
    unit + "^" + power
}

case class Unit(nums: Seq[PoweredUnit], denoms: Seq[PoweredUnit]) {

  import Unit._

  def + (that: Unit): Unit = checkIfSame(that)
  def - (that: Unit): Unit = checkIfSame(that)
  def * (that: Unit): Unit = {
    val (newNums, newDenoms) = simplify( optimize(nums, that.nums), optimize(denoms, that.denoms) )
    Unit(newNums, newDenoms)
  }
  def / (that: Unit): Unit = {
    val (newNums, newDenoms) = simplify( optimize(nums, that.denoms), optimize(denoms, that.nums) )
    Unit(newNums, newDenoms)
  }

  private def checkIfSame(that: Unit): Unit = {
    require(that == this)
    this
  }

  override def toString(): String =
    List( nums.mkString(" "), denoms.mkString(" ") ).filterNot( _.isEmpty ).mkString(" / ")

}

object Unit {

  private def invert(unit: PoweredUnit) = unit.copy( power = -1 * unit.power )

  private def optimize(first: Seq[PoweredUnit], second: Seq[PoweredUnit]) = combine(first, second) ( _ * _ )

  private def simplify(first: Seq[PoweredUnit], second: Seq[PoweredUnit]) = {
    val (simplifiedNums, simplifiedDenoms) = optimize(first, second map ( invert )) partition ( _.power >= 0 )
    (simplifiedNums.filterNot( _.power == 0), simplifiedDenoms.map( invert ))
  }

  private def combine(first: Seq[PoweredUnit], second: Seq[PoweredUnit])(fn: (PoweredUnit, PoweredUnit) => PoweredUnit): Seq[PoweredUnit] = {
    if (second.isEmpty) first
    else {
      val (same, different) = second.partition( unit => first.exists( _.hasSameUnit(unit) ))
      val updated = first.foldLeft(Seq[PoweredUnit]()) { (current, num) =>
        ( same.find( num.hasSameUnit ) map ( fn(num, _: PoweredUnit) ) getOrElse num ) +: current
      }
      updated ++ different
    }
  }

  def apply(unit: BaseUnit, power: Int = 1): Unit =
    Unit(Seq(PoweredUnit(unit, power)), Nil)
}

case class ValueWithUnit(value: Double, unit: Unit) {

  def + (that: ValueWithUnit): ValueWithUnit = ValueWithUnit(value + that.value, unit + that.unit)
  def - (that: ValueWithUnit): ValueWithUnit = ValueWithUnit(value - that.value, unit - that.unit)
  def * (that: ValueWithUnit): ValueWithUnit = ValueWithUnit(value * that.value, unit * that.unit)
  def / (that: ValueWithUnit): ValueWithUnit = ValueWithUnit(value / that.value, unit / that.unit)

  override def toString(): String = value + " " + unit
}

object UnitMathExample extends App {

  val u1 = ( Unit(Meter, 2) * Unit(Kilogram) ) / ( Unit(Cost, 2) * Unit(StorageBytes) )
  val u2 = ( Unit(Cost, 1) * Unit(Kilogram) * Unit(Meter) )

  println(" UNITS: --> ")
  println("1st unit: " + u1)
  println("2nd unit: " + u2)

  println(" compatible --> ")
  println("add: " + Try(u1 + u1) )
  println("sub: " + Try(u1 + u1) )
  println("multiply: " + Try(u1 * u2) )
  println("divide: " + Try(u1 / u2) )

  val v1 = ValueWithUnit( 2, u1 )
  val v2 = ValueWithUnit( 10, u2 )
  val v3 = ValueWithUnit( 1.5, u1 )

  println(" VALUES --> ")
  println("1st value: " + v1)
  println("2nd value: " + v2)
  println("3rd value: " + v3)

  println(" same --> ")
  println("add: " + Try(v1 + v3))
  println("sub: " + Try(v1 - v3))
  println("multiply: " + Try(v1 * v3))
  println("divide: " + Try(v1 / v3))

  println(" different --> ")
  println("add: " + Try(v1 + v2))
  println("sub: " + Try(v1 - v2))
  println("multiply: " + Try(v1 * v2))
  println("divide: " + Try(v1 / v2))
}


