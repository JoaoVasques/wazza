name := "bootstrap"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++=  Seq(
  "org.mongodb" % "casbah-commons_2.10" % "2.8.1",
  "org.mongodb" % "casbah_2.10" % "2.8.1",
  "org.slf4j" % "slf4j-api" % "1.7.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.1"  // for any java classes looking for this
)

fork := true

javaOptions in run ++= Seq("-Xms1024M", "-Duser.timezone=GMT")

retrieveManaged := true

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

// For stable releases
resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"

