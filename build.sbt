name := "Wazza"

version := "alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0",
  "com.amazonaws" % "aws-java-sdk" % "1.9.8",
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.7",
  "commons-validator" % "commons-validator" % "1.4.0",
  "commons-codec" % "commons-codec" % "1.10",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.webjars" % "underscorejs" % "1.7.0",
  "org.webjars" % "webjars-play_2.10" % "2.2.2-1",
  "org.webjars" % "angularjs" % "1.2.27",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "jquery" % "1.11.1",
  "org.webjars" % "angular-ui-bootstrap" % "0.12.0",
  "org.webjars" % "angular-ui-router" % "0.2.13",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "chartjs" % "1.0.1-beta.4",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka22",
  "org.mongodb" % "casbah_2.10" % "2.7.4",
  "org.mongodb" % "casbah-commons_2.10" % "2.7.4"
)

libraryDependencies ++= dependencies

lazy val mySettings = Seq(
  scalacOptions ++= Seq("-feature", "-language:reflectiveCalls"),
  scalacOptions ++= Seq("-feature", "-language:postfixOps")
)

// Projects
lazy val home = project.in(file("."))
  .aggregate(dashboardModule,
    userModule,
    applicationModule,
    securityModule,
    awsModule,
    apiModule,
    persistenceModule,
    analyticsModule)
  .dependsOn(dashboardModule,
    userModule,
    applicationModule,
    securityModule,
    awsModule,
    apiModule,
    persistenceModule,
    analyticsModule)
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
              .dependsOn(persistenceModule, securityModule, awsModule, userModule)
              .settings(mySettings: _*)

lazy val securityModule = play.Project("security",
                    version.toString,
                    dependencies,
                    path = file("modules/SecurityModule")
              )
              .dependsOn(persistenceModule)
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

lazy val analyticsModule = play.Project("analytics",
  version.toString,
  dependencies,
  path = file("modules/AnalyticsModule")
)
  .dependsOn(userModule, applicationModule, persistenceModule, securityModule)
  .settings(mySettings: _*)

play.Project.playScalaSettings
