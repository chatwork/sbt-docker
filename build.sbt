import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype.autoImport._

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

val sbtCrossVersion = sbtVersion in pluginCrossBuild

scalaVersion := (CrossVersion partialVersion sbtCrossVersion.value match {
  case Some((0, 13)) => "2.10.6"
  case Some((1, _))  => "2.12.4"
  case _             => sys error s"Unhandled sbt version ${sbtCrossVersion.value}"
})

crossSbtVersions := Seq("0.13.16", "1.0.4")

sonatypeProfileName := "com.chatwork"

organization := "com.chatwork"

publishMavenStyle := true

publishArtifact in Test := false

publishTo := sonatypePublishTo.value

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
  "com.spotify" % "docker-client" % "2.7.7",
  // "com.spotify" % "docker-client" % "8.15.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.freemarker" % "freemarker" % "2.3.28"
)

credentials += Credentials((baseDirectory in LocalRootProject).value / ".credentials")