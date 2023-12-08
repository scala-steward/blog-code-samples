name := "blog-code-samples"

val scala2Version = "2.13.12"
val scala3Version = "3.3.1"

lazy val cats = project
  .in(file("cats"))
  .settings(
    name := "cats",
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
val diffxVersion = "0.9.0"

lazy val scala3 = project
  .in(file("scala3"))
  .configs(ManualTestConfig)
  .settings(
    name := "scala3",
    scalaVersion := scala3Version,
    manualTestSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.diffx" %% "diffx-core" % diffxVersion,
      "org.scalatest" %% "scalatest-flatspec" % "3.2.17" % Test,
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-test" % "2.0.19" % Test
    )
  )

val testContainersVersion = "0.41.0"

lazy val scala2 = project
  .in(file("scala2"))
  .configs(IntegrationTest)
  .settings(
    name := "scala2",
    scalaVersion := scala2Version,
    Test / fork := true,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainersVersion % Test,
      "org.tpolecat" %% "skunk-core" % "0.6.2",
      "org.scalatest" %% "scalatest-flatspec" % "3.2.17" % "test,it",
      "org.postgresql" % "postgresql" % "42.7.1",
      "org.reactivemongo" %% "reactivemongo" % "1.0.10",
      "com.dimafeng" %% "testcontainers-scala-mongodb" % testContainersVersion % Test,
      "org.wvlet.airframe" %% "airframe-ulid" % "23.11.3",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % Test,
      "com.lihaoyi" %% "fansi" % "0.4.0"
    )
  )

lazy val ManualTestConfig = config("manual") extend (Test)
lazy val manualTestSettings = inConfig(ManualTestConfig)(Defaults.testSettings)

lazy val startEmbedMongo =
  taskKey[Unit]("Start embedded mongodb for the CI tests")
lazy val port = 8765
startEmbedMongo := {
  val log = streams.value.log
  log.info("Starting the embedded mongodb on port: " + port)
  EmbeddedMongoInstance.start(port)
}
lazy val stopEmbedMongo = taskKey[Unit]("Stop embedded mongodb after CI tests")
stopEmbedMongo := {
  val log = streams.value.log
  log.info("Stopping the embedded mongodb on port: " + port)
  EmbeddedMongoInstance.stop(port)
}
addCommandAlias("mongoTests", ";startEmbedMongo;it:test;stopEmbedMongo")

Global / onLoad ~= { state =>
  import java.nio.file._
  import java.time.LocalDateTime
  val dummyFile = Paths.get(".dummy")
  Files.write(
    dummyFile,
    s"This is a dummy content written on startup : ${LocalDateTime.now}".getBytes
  )
  state
}
