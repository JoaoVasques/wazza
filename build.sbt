import play.Project._

name := "Wazza"

version := "pre-alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1"
)

libraryDependencies ++= dependencies

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

routesImport += "se.radley.plugin.salat.Binders._"

templatesImport += "org.bson.types.ObjectId"

// Projects
lazy val home = project.in(file(".")).dependsOn(editor, stores).aggregate(editor, stores)

lazy val editor = play.Project("editor", "pre-alpha", dependencies, path = file("modules/editor")).settings().dependsOn(stores)

lazy val stores = play.Project("stores", "pre-alpha", dependencies, path = file("modules/stores"))

play.Project.playScalaSettings
