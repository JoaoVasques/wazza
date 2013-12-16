import play.Project._

name := "Wazza"

version := "pre-alpha"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2"
)

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

routesImport += "se.radley.plugin.salat.Binders._"

templatesImport += "org.bson.types.ObjectId"

// Projects
lazy val home = project.in(file(".")).dependsOn(editor)

lazy val editor = play.Project("editor", "pre-alpha", path = file("modules/editor")).settings().dependsOn(stores)

lazy val stores = play.Project("stores", "pre-alpha", path = file("modules/stores"))

play.Project.playScalaSettings
