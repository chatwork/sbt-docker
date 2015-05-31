package com.chatwork.sbt.docker

import sbt._

object SbtDockerKeys extends SbtDockerKeys

trait SbtDockerKeys {

  val docker = taskKey[Unit]("docker")

  val sourceFiles = settingKey[Seq[File]]("source-files")

  val copySourceFiles = taskKey[Set[File]]("copy-source-files")

  val build = taskKey[Option[String]]("build")

  val buildDirectory = settingKey[File]("build-directory")

}