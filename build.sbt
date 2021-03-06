import sbt._

lazy val root = project("scala-template-backwards", file("."))
  .settings(description := "Scala Template by Backwards")
  .aggregate(main, macros)

lazy val codeGen = taskKey[Unit]("Generate my file")

lazy val macros = project("macros", file("macros"))
  .settings(
    codeGen := (runMain in Compile).toTask(" com.backwards.macros.LetsGo").value
  )

lazy val main = project("main", file("main"))
  .dependsOn(macros)
  .settings(javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf"))

def project(id: String, base: File): Project =
  Project(id, base)
    .enablePlugins(JavaAppPackaging)
    .configs(IntegrationTest extend Test)
    .settings(inConfig(IntegrationTest extend Test)(Defaults.testSettings))
    .settings(Defaults.itSettings)
    .settings(
      resolvers ++= Seq(
        Resolver sonatypeRepo "releases",
        Resolver sonatypeRepo "snapshots",
        "jitpack" at "https://jitpack.io",
        "Artima Maven Repository" at "https://repo.artima.com/releases"
      ),
      scalaVersion := BuildProperties("scala.version"),
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards",
      name := id,
      autoStartServer := false,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      libraryDependencies ++= Dependencies(),
      scalacOptions ++= Seq(
        "-encoding", "utf8",
        "-deprecation",
        "-unchecked",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ymacro-annotations",
        "-Xfatal-warnings",
        "-Ywarn-value-discard"
      ),
      fork := true,
      publishArtifact in Test := true,
      publishArtifact in IntegrationTest := true,
      addArtifact(artifact in (IntegrationTest, packageBin), packageBin in IntegrationTest).settings
    )