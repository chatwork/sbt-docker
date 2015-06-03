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
    login in docker := false,
    emailAddress in docker := "",
    userName in docker := "",
    password in docker := "",
    buildOptions in docker := Set.empty[BuildOptions.Value],
    build in docker <<= dockerBuildTask dependsOn (copySourceFiles in docker),
    copySourceFiles in docker <<= copySourceFilesTask,
    dockerfileTemplate in docker := (sourceDirectory in docker).value / "Dockerfile.ftl",
    dockerfile in docker := (sourceDirectory in docker).value / "Dockerfile",
    templateContext in docker := Map(
      "name" -> (name in thisProjectRef).value,
      "version" -> (version in thisProjectRef).value
    ),
    generateDockerfile in docker <<= generateDockerfileTask,
    push in docker <<= dockerPushTask,
    pull in docker <<= dockerPullTask,
    list in docker <<= dockerListImagesTask,
    start in docker <<= dockerStartTask dependsOn (copySourceFiles in docker),
    startAndWait in docker <<= dockerStartAndWaitTask dependsOn (copySourceFiles in docker)
  )

}
