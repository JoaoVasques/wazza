lazy val appName = "Wazza"

lazy val appVersion = "alpha"

scalaVersion := "2.10.4"

lazy val dependencies = Seq(
  anorm,
  cache,
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "com.amazonaws" % "aws-java-sdk" % "1.9.8",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.7",
  "commons-validator" % "commons-validator" % "1.4.0",
  "commons-codec" % "commons-codec" % "1.10",
  "org.webjars" % "jquery" % "1.11.1",
  "org.webjars" % "webjars-play_2.10" % "2.3.0-2",
  "org.webjars" % "angularjs" % "1.2.27",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
  "org.webjars" % "angular-ui-router" % "0.2.13",
  "org.webjars" % "underscorejs" % "1.7.0",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "chartjs" % "1.0.1-beta.4",
  "org.mongodb" % "casbah_2.10" % "2.7.4",
  "org.mongodb" % "casbah-commons_2.10" % "2.7.4",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
)

libraryDependencies ++= dependencies

lazy val mySettings = Seq(
  scalacOptions ++= Seq("-feature", "-language:reflectiveCalls"),
  scalacOptions ++= Seq("-feature", "-language:postfixOps")
)

lazy val dashboard = Project("dashboard", file("modules/dashboard"))
  .enablePlugins(play.PlayScala)
  .dependsOn(user, application)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val user = Project("user", file("modules/user"))
  .enablePlugins(play.PlayScala)
  .dependsOn(security, persistence)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val application = Project("application", file("modules/application"))
  .enablePlugins(play.PlayScala)
  .dependsOn(persistence, security, aws, user)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val security = Project("security", file("modules/security"))
  .enablePlugins(play.PlayScala)
  .dependsOn(persistence)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val aws = Project("aws", file("modules/aws"))
  .enablePlugins(play.PlayScala)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val api = Project("api", file("modules/api"))
  .enablePlugins(play.PlayScala)
  .dependsOn(security, aws, user, application)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val persistence = Project("persistence", file("modules/persistence"))
  .enablePlugins(play.PlayScala)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

lazy val analytics = Project("analytics",file("modules/analytics"))
  .enablePlugins(play.PlayScala)
  .dependsOn(user, application, persistence, security)
  .settings(version := appVersion, libraryDependencies ++= dependencies)

// Root
lazy val home = Project(appName, file("."))
  .enablePlugins(play.PlayScala)
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
  .settings(version := appVersion, libraryDependencies ++= dependencies)

sources in doc in Compile := List()
