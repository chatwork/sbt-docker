package com.chatwork.sbt.docker

import sbt._
import sbt.plugins.IvyPlugin

object SbtDockerPlugin extends AutoPlugin {

  override def trigger = allRequirements

  override def requires: Plugins = IvyPlugin

  object autoImport extends SbtDockerKeys

  override def projectSettings: Seq[Def.Setting[_]] = Seq(

  )

}
