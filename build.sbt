lazy val appName = "Wazza"

lazy val appVersion = "alpha"

scalaVersion := "2.10.4"

lazy val dependencies = Seq(
  anorm,
  cache,
  ws,
  "com.amazonaws" % "aws-java-sdk" % "1.9.13",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0",
  "com.google.inject" % "guice" % "3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.8",
  "com.tzavellas" %% "sse-guice" % "0.7.2",
  "commons-codec" % "commons-codec" % "1.10",
  "commons-validator" % "commons-validator" % "1.4.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.mongodb" % "casbah-commons_2.10" % "2.7.4",
  "org.mongodb" % "casbah_2.10" % "2.7.4",
  "org.webjars" % "angular-local-storage" % "0.1.5",
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
  "org.webjars" % "angular-ui-router" % "0.2.13",
  "org.webjars" % "angularjs" % "1.2.27",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "chartjs" % "1.0.1-beta.4",
  "org.webjars" % "jquery" % "1.11.2",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "numeral-js" % "1.5.3-1",
  "org.webjars" % "underscorejs" % "1.7.0-1",
  "org.webjars" % "webjars-play_2.10" % "2.3.0-2"
)


libraryDependencies ++= dependencies

resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

lazy val mySettings = Seq("-unchecked", "-deprecation", "-feature", "-language:reflectiveCalls", "-language:postfixOps", "-optimize")

lazy val common = Project("common", file("modules/common"))
  .enablePlugins(play.PlayScala)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val dashboard = Project("dashboard", file("modules/dashboard"))
  .enablePlugins(play.PlayScala)
  .dependsOn(user, application)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val user = Project("user", file("modules/user"))
  .enablePlugins(play.PlayScala)
  .dependsOn(security, persistence, common)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val application = Project("application", file("modules/application"))
  .enablePlugins(play.PlayScala)
  .dependsOn(persistence, security, aws, user, notifications)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val security = Project("security", file("modules/security"))
  .enablePlugins(play.PlayScala)
  .dependsOn(persistence)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val aws = Project("aws", file("modules/aws"))
  .enablePlugins(play.PlayScala)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val api = Project("api", file("modules/api"))
  .enablePlugins(play.PlayScala)
  .dependsOn(security, aws, user, application)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val persistence = Project("persistence", file("modules/persistence"))
  .enablePlugins(play.PlayScala)
  .dependsOn(common)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val analytics = Project("analytics",file("modules/analytics"))
  .enablePlugins(play.PlayScala)
  .dependsOn(user, application, persistence, security)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

lazy val notifications = Project("notifications",file("modules/notifications"))
  .enablePlugins(play.PlayScala)
  .dependsOn(common)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

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
    analytics,
    common,
    notifications)
  .dependsOn(dashboard,
    user,
    application,
    security,
    aws,
    api,
    persistence,
    analytics,
    common,
    notifications)
  .settings(scalacOptions ++= mySettings, version := appVersion, libraryDependencies ++= dependencies)

sources in doc in Compile := List()

