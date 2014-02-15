name := "Wazza"

version := "pre-alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "se.radley" %% "play-plugins-salat" % "1.4.0",
//  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "org.webjars" % "webjars-play_2.10" % "2.2.0",
  "org.webjars" % "angularjs" % "1.2.6",
  "org.webjars" % "bootstrap" % "3.0.3",
  "commons-validator" % "commons-validator" % "1.4.0",
  "com.github.nscala-time" %% "nscala-time" % "0.6.0",
  "org.webjars" % "underscorejs" % "1.5.2-1",
  "com.amazonaws" % "aws-java-sdk" % "1.6.12",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.webjars" % "angular-ui-bootstrap" % "0.10.0"
)

libraryDependencies ++= dependencies

//resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

templatesImport += "org.bson.types.ObjectId"

templatesImport += "models.user._"

templatesImport += "controllers.user._"

lazy val mySettings = Seq(
    javaOptions in run += "-Dconfig.file=conf/dev/application_dev.conf",
    routesImport ++= Seq("se.radley.plugin.salat.Binders._"),
    scalacOptions ++= Seq("-feature", "-language:reflectiveCalls")
)

// Projects
lazy val home = project.in(file("."))
  .aggregate(dashboardModule, userModule, applicationModule, securityModule, photosModule, awsModule, apiModule, persistenceModule)
  .dependsOn(dashboardModule, userModule, applicationModule, securityModule, photosModule, awsModule, apiModule, persistenceModule)
  .settings(mySettings: _*)

lazy val dashboardModule = play.Project("dashboard",
                    version.toString,
                    dependencies,
                    path = file("modules/DashboardModule")
                )
                .dependsOn(userModule, applicationModule)
                .settings(mySettings: _*)

lazy val userModule = play.Project("user",
                    version.toString,
                    dependencies,
                    path = file("modules/UserModule")
              )
              .dependsOn(securityModule, persistenceModule)
              .settings(mySettings: _*)

lazy val applicationModule = play.Project("application",
                    version.toString,
                    dependencies,
                    path = file("modules/ApplicationModule")
              )
              .dependsOn(securityModule, photosModule, awsModule, userModule, persistenceModule)
              .settings(mySettings: _*)

lazy val securityModule = play.Project("security",
                    version.toString,
                    dependencies,
                    path = file("modules/SecurityModule")
              )
              .settings(mySettings: _*)

lazy val photosModule = play.Project("photos",
                    version.toString,
                    dependencies,
                    path = file("modules/PhotosModule")
              )
              .settings(mySettings: _*)

lazy val awsModule = play.Project("aws",
                    version.toString,
                    dependencies,
                    path = file("modules/AWSModule")
              )
              .settings(mySettings: _*)

lazy val apiModule = play.Project("api",
                    version.toString,
                    dependencies,
                    path = file("modules/ApiModule")
              )
              .dependsOn(securityModule, awsModule, userModule, applicationModule)
              .settings(mySettings: _*)


lazy val persistenceModule = play.Project("persistence",
                  version.toString,
                  dependencies,
                  path = file("modules/PersistenceModule")
              )
              .settings(mySettings: _*)


play.Project.playScalaSettings
