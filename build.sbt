import sbt.ScriptedPlugin._

import scalariform.formatter.preferences._

scalaVersion := "2.10.5"

sonatypeProfileName := "com.chatwork"

organization in ThisBuild := "com.chatwork"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := {
    _ => false
}

pomExtra := {
  <url>https://github.com/chatwork/sbt-docker</url>
    <licenses>
      <license>
        <name>The MIT License</name>
        <url>http://opensource.org/licenses/MIT</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:chatwork/sbt-docker.git</url>
      <connection>scm:git:github.com/chatwork/sbt-docker</connection>
      <developerConnection>scm:git:git@github.com:chatwork/sbt-docker.git</developerConnection>
    </scm>
    <developers>
      <developer>
        <id>cw-junichikato</id>
        <name>Junichi Kato</name>
      </developer>
    </developers>
}

name := "sbt-docker"

sbtPlugin := true

resolvers ++= Seq(
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
)

scalariformSettings

ScalariformKeys.preferences :=
  ScalariformKeys.preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)

