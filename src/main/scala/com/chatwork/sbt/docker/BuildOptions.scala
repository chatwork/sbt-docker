package com.chatwork.sbt.docker

object BuildOptions extends BuildOptions

trait BuildOptions extends Enumeration {
  val Quiet, NoCache, NoRm, ForceRm = Value
}
