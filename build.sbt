name := "Wazza"

version := "pre-alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "se.radley" %% "play-plugins-salat" % "1.4.0",
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "org.webjars" % "webjars-play_2.10" % "2.2.0",
  "org.webjars" % "angularjs" % "1.2.5",
  "org.webjars" % "bootstrap" % "3.0.3"
)

libraryDependencies ++= dependencies

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

routesImport += "se.radley.plugin.salat.Binders._"

templatesImport += "org.bson.types.ObjectId"

lazy val mySettings = Seq(
    Keys.fork in run := true,
    javaOptions in run += "-Dconfig.file=conf/dev/application_dev.conf"
)

// Projects
lazy val home = project.in(file("."))
                .aggregate(editor, stores)
                .dependsOn(editor, stores)
                .settings(mySettings: _*)

lazy val editor = play.Project("editor",
                    version.toString,
                    dependencies, path = file("modules/editor")
                )
                .settings(mySettings: _*)

lazy val stores = play.Project(
                    "stores",
                    version.toString,
                    dependencies, path = file("modules/stores")
                )
                .dependsOn(editor)
                .settings(mySettings: _*)

play.Project.playScalaSettings
