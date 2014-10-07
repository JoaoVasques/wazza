name := "Wazza"

version := "pre-alpha"

lazy val dependencies = Seq(
  anorm,
  cache,
  "org.webjars" % "jquery" % "1.11.1",
  "com.google.inject" % "guice" % "3.0",
  "com.tzavellas" % "sse-guice" % "0.7.1",
  "org.webjars" % "webjars-play_2.10" % "2.2.2-1",
  "org.webjars" % "angularjs" % "1.2.25",
  "org.webjars" % "bootstrap" % "3.2.0",
  "commons-validator" % "commons-validator" % "1.4.0",
  "com.github.nscala-time" %% "nscala-time" % "1.0.0",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "com.amazonaws" % "aws-java-sdk" % "1.7.9",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-codec" % "commons-codec" % "1.9",
  "org.mongodb" % "casbah_2.10" % "2.7.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  "org.webjars" % "angular-ui-bootstrap" % "0.11.0-3",
  "org.webjars" % "angular-ui-router" % "0.2.11",
  "org.webjars" % "momentjs" % "2.8.3",
  "org.webjars" % "chartjs" % "1.0.1-beta.4",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)

libraryDependencies ++= dependencies

templatesImport += "org.bson.types.ObjectId"

templatesImport += "models.user._"

templatesImport += "controllers.user._"

lazy val mySettings = Seq(
    javaOptions in run += "-Dconfig.file=conf/dev/application_dev.conf",
    scalacOptions ++= Seq("-feature", "-language:reflectiveCalls")
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
    recommendationModule,
    analyticsModule)
  .dependsOn(dashboardModule,
    userModule,
    applicationModule,
    securityModule,
    awsModule,
    apiModule,
    persistenceModule,
    recommendationModule,
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
              .dependsOn(securityModule, awsModule, userModule, persistenceModule)
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
              .dependsOn(securityModule, awsModule, userModule, applicationModule, recommendationModule)
              .settings(mySettings: _*)


lazy val persistenceModule = play.Project("persistence",
                  version.toString,
                  dependencies,
                  path = file("modules/PersistenceModule")
              )
              .settings(mySettings: _*)

lazy val recommendationModule = play.Project("recommendation",
  version.toString,
  dependencies,
  path = file("modules/RecommendationModule")
)
  .dependsOn(userModule, applicationModule, persistenceModule)
  .settings(mySettings: _*)

lazy val analyticsModule = play.Project("analytics",
  version.toString,
  dependencies,
  path = file("modules/AnalyticsModule")
)
  .dependsOn(userModule, applicationModule, persistenceModule, securityModule)
  .settings(mySettings: _*)

play.Project.playScalaSettings
