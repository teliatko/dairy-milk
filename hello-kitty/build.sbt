val catsVersion = "0.1.2"
val catsAll = "org.spire-math" %% "cats" % catsVersion
val macroParaside = compilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
val kindProjector = compilerPlugin("org.spire-math" % "kind-projector" % "0.6.3" cross CrossVersion.binary)
val resetAllAttrs = "org.scalamacros" %% "resetallattrs" % "1.0.0-M1"

val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.4"

lazy val hello_kitty = (project in file(".")).
  settings(
    name := "Hello Kitty",
    description := "Project to try Cats (inspired by Hearding Cats from Eugene Yokota, see http://eed3si9n.com/herding-cats/)",
    organization := "codes.teliatko",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:_"
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    ),
    libraryDependencies ++= Seq(
      catsAll,
      scalatest % Test, scalacheck % Test,
      macroParaside, kindProjector, resetAllAttrs
    )
  )
