name := "Wazza"

version := "alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "org.webjars" % "jquery" % "1.11.1",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "org.webjars" % "webjars-play_2.10" % "2.2.2-1",
  "org.webjars" % "angularjs" % "1.2.26",
  "org.webjars" % "bootstrap" % "3.2.0",
  "commons-validator" % "commons-validator" % "1.4.0",
  "com.github.nscala-time" %% "nscala-time" % "1.0.0",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "com.amazonaws" % "aws-java-sdk" % "1.9.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-codec" % "commons-codec" % "1.9",
  "org.mongodb" % "casbah_2.10" % "2.7.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  "org.webjars" % "angular-ui-bootstrap" % "0.11.2",
  "org.webjars" % "angular-ui-router" % "0.2.11-1",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "chartjs" % "1.0.1-beta.4",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "org.mongodb" % "casbah-commons_2.10" % "2.7.3"
)

libraryDependencies ++= dependencies

lazy val mySettings = Seq(
  scalacOptions ++= Seq("-feature", "-language:reflectiveCalls"),
  scalacOptions ++= Seq("-feature", "-language:postfixOps")
)

lazy val dashboard = play.Project("dashboard",
  version.toString,
  dependencies,
  path = file("modules/dashboard")
)
  .dependsOn(user, application)
  .settings(mySettings: _*)

lazy val user = play.Project("user",
  version.toString,
  dependencies,
  path = file("modules/user")
)
  .dependsOn(security, persistence)
  .settings(mySettings: _*)

lazy val application = play.Project("application",
  version.toString,
  dependencies,
  path = file("modules/application")
)
  .dependsOn(persistence, security, aws, user)
  .settings(mySettings: _*)

lazy val security = play.Project("security",
  version.toString,
  dependencies,
  path = file("modules/security")
)
  .dependsOn(persistence)
  .settings(mySettings: _*)

lazy val aws = play.Project("aws",
  version.toString,
  dependencies,
  path = file("modules/aws")
)
  .settings(mySettings: _*)

lazy val api = play.Project("api",
  version.toString,
  dependencies,
  path = file("modules/api")
)
  .dependsOn(security, aws, user, application)
  .settings(mySettings: _*)


lazy val persistence = play.Project("persistence",
  version.toString,
  dependencies,
  path = file("modules/persistence")
)
  .settings(mySettings: _*)

lazy val analytics = play.Project("analytics",
  version.toString,
  dependencies,
  path = file("modules/analytics")
)
  .dependsOn(user, application, persistence, security)
  .settings(mySettings: _*)

// Root
lazy val home = project.in(file("."))
  .aggregate(dashboard,
    user,
    application,
    security,
    aws,
    api,
    persistence,
    analytics)
  .dependsOn(dashboard,
    user,
    application,
    security,
    aws,
    api,
    persistence,
    analytics)
  .settings(mySettings: _*)

play.Project.playScalaSettings

sources in doc in Compile := List()
