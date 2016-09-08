// ---------
// Example 1
// ---------
// source: http://danielwestheide.com/blog/2013/02/13/the-neophytes-guide-to-scala-part-13-path-dependent-types.html

// Common class
class Franchise(name: String) {
  // Inner class, trait or type - related only to specific instance of [[Franchise]]
  // It's 'path dependent' from instance of Franchise
  case class Character(name: String)
  def createFanFictionWith(
    lovestruck: Character,
    objectOfDesire: Character): (Character, Character) = (lovestruck, objectOfDesire)
}

// Client of Franchise, show effects of path depemndent types
object Client {
  // 2 instances of Franchise
  val starTrek = new Franchise("Star Trek")
  val starWars = new Franchise("Star Wars")
  // 2 characters from 'starTrek' instance, they are type compatible
  val quark = starTrek.Character("Quark")
  val jadzia = starTrek.Character("Jadzia Dax")
  // 2 characters from 'starWars' instance, they are type compatible, but type incompatible with 'starTrek' ones.
  val luke = starWars.Character("Luke Skywalker")
  val yoda = starWars.Character("Yoda")

  // This will compile, all instances are from same Franchise
  starTrek.createFanFictionWith(lovestruck = quark, objectOfDesire = jadzia)
  starWars.createFanFictionWith(lovestruck = luke, objectOfDesire = yoda)

  // This does not compile type of Character class in argument is incompatible
  // starTrek.createFanFictionWith(lovestruck = jadzia, objectOfDesire = luke)

  // Example of 'dependent method types'
  // Same as [[Franchise.createFanFictionWith]] but defined outside
  def createFanFiction(f: Franchise)(lovestruck: f.Character, objectOfDesire: f.Character) = // f.Character reflects path dependency
  (lovestruck, objectOfDesire)
}

// ---------
// Example 1
// ---------
// source: http://danielwestheide.com/blog/2013/02/13/the-neophytes-guide-to-scala-part-13-path-dependent-types.html
// Shows combination of 'path dependent types' and abstract types

object AwesomeDB {
  abstract class Key(name: String) {
    type Value // absract type which is path dependent from instance of Key class
  }
}

import AwesomeDB.Key

class AwesomeDB {
  import collection.mutable.Map

  // Internal map to store key/value pairs
  private val data = Map.empty[Key, Any]

  // Accessors to values via keys, methods will be typesafe
  // i.e. something stored under key w/Value X cannot be rettrieved via key w/Value Y
  def get(key: Key): Option[key.Value] = data.get(key).asInstanceOf[Option[key.Value]]
  def set(key: Key)(value: key.Value): Unit = data.update(key, value)
}

// Definition of Keys, they will assure
trait IntValued extends Key {
 type Value = Int
}
trait StringValued extends Key {
  type Value = String
}
// Holds types of keys for convenience
object Keys {
  val intKey = new Key("intKey") with IntValued
  val stringKey = new Key("stringKey") with StringValued
}

object Client2 {
  val dataStore = new AwesomeDB // create DB
  dataStore.set(Keys.intKey)(23) // store something as Int (using intKey)
  val i: Option[Int] = dataStore.get(Keys.intKey) // retrieve it as Int
  // dataStore.set(Keys.intKey)("23") // does not compile, cannot be retrieved via stringKey
}
