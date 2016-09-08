name := "Various"

description := """
|This project contains various learing topics.
|It's collection of several small snipets and classes, loosely organized into packages. 
|""".stripMargin

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-feature", "-language:higherKinds")

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  , "org.scalaz" %% "scalaz-core" % "7.1.3"
  , "org.spire-math" %% "cats" % "0.1.2"
)
