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
  "org.webjars" % "bootstrap" % "3.0.3",
  "commons-validator" % "commons-validator" % "1.4.0"
)

libraryDependencies ++= dependencies

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

routesImport += "se.radley.plugin.salat.Binders._"

templatesImport += "org.bson.types.ObjectId"

templatesImport += "models.user._"

templatesImport += "controllers.user._"

lazy val mySettings = Seq(
    Keys.fork in run := true,
    javaOptions in run += "-Dconfig.file=conf/dev/application_dev.conf"
)

// Projects
lazy val home = project.in(file("."))
                .aggregate(editorModule, storesModule, userModule, applicationModule, securityModule)
                .dependsOn(editorModule, storesModule, userModule, applicationModule)
                .settings(mySettings: _*)

lazy val editorModule = play.Project("editor",
                    version.toString,
                    dependencies,
                    path = file("modules/EditorModule")
                )
                .dependsOn(userModule)
                .settings(mySettings: _*)

lazy val storesModule = play.Project("stores",
                    version.toString,
                    dependencies,
                    path = file("modules/StoresModule")
                )
                .dependsOn(editorModule, userModule)
                .settings(mySettings: _*)

lazy val userModule = play.Project("user",
                    version.toString,
                    dependencies,
                    path = file("modules/UserModule")
              )
              .dependsOn(applicationModule)
              .settings(mySettings: _*)

lazy val applicationModule = play.Project("application",
                    version.toString,
                    dependencies,
                    path = file("modules/ApplicationModule")
              )
              .dependsOn(securityModule)
              .settings(mySettings: _*)

lazy val securityModule = play.Project("security",
                    version.toString,
                    dependencies,
                    path = file("modules/SecurityModule")
              )
              .settings(mySettings: _*)


play.Project.playScalaSettings
