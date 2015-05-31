package com.chatwork.sbt.docker

import sbt.Keys._
import sbt._
import sbt.plugins.IvyPlugin

object SbtDockerPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires: Plugins = IvyPlugin

  object autoImport extends SbtDockerKeys

  import SbtDocker._
  import SbtDockerKeys._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    name in docker := (name in thisProjectRef).value,
    sourceDirectory in docker := baseDirectory.value / "docker",
    buildDirectory in docker := baseDirectory.value / "target" / "docker",
    sourceFiles in docker := Seq(),
    build in docker <<= dockerBuildTask dependsOn(copySourceFiles in docker),
    copySourceFiles in docker  <<= copySourceFilesTask
  )

}
