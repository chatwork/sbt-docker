package com.chatwork.sbt.docker

import sbt._

object SbtDockerKeys extends SbtDockerKeys

trait SbtDockerKeys {

  val docker = taskKey[Unit]("docker")

  val login = settingKey[Boolean]("login")

  val emailAddress = settingKey[String]("email-address")

  val userName = settingKey[String]("user-name")

  val password = settingKey[String]("password")

  val sourceFiles = settingKey[Seq[File]]("source-files")

  val copySourceFiles = taskKey[Set[File]]("copy-source-files")

  val build = taskKey[Option[String]]("build")

  val buildOptions = settingKey[Set[BuildOptions.Value]]("build-options")

  val buildDirectory = settingKey[File]("build-directory")

}