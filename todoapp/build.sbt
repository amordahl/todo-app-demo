val scala3Version = "3.7.3"

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    name                   := "TodoApp",
    version                := "0.1.0-SNAPSHOT",
    scalaVersion           := scala3Version,
    semanticdbEnabled      := true,
    semanticdbVersion      := scalafixSemanticdb.revision,
    Compile / doc / target := file("docs"),
    scalacOptions ++= Seq(
      "-Wunused:all",
      "-Wnonunit-statement",
      "-Wvalue-discard",
      "-deprecation",
      "-feature",
      "-source:future"
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi"         %% "cask"           % "0.9.7",
      "io.github.rediscala" %% "rediscala"      % "2.0.1",
      "com.typesafe.slick"  %% "slick"          % "3.5.2",
      "com.typesafe.slick"  %% "slick-hikaricp" % "3.5.2",
      "org.postgresql"       % "postgresql"     % "42.7.3"
    ),
    // Test Dependencies
    libraryDependencies += "org.scalameta" %% "munit" % "1.1.0" % Test,
    libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "1.1.0" % Test
  )
