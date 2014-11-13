name := "sse-tweetmap"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

val akka = "2.3.6"

libraryDependencies ++= Seq(
  ws,
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "angularjs" % "1.2.16",
  "org.webjars" % "angular-leaflet-directive" % "0.7.6",
  "com.typesafe.akka" %% "akka-testkit" % akka % "test",
  "com.typesafe.akka" %% "akka-cluster" % akka,
  "com.typesafe.akka" %% "akka-contrib" % akka
)

addCommandAlias("rb", "runMain backend.MainTweetLoader 2552 -Dakka.remote.netty.tcp.port=2552 -Dakka.cluster.roles.0=backend-loader")
