package com.chatwork.sbt.docker

import com.chatwork.sbt.docker.SbtDockerKeys._
import com.spotify.docker.client.messages.ProgressMessage
import com.spotify.docker.client.{ ProgressHandler, DefaultDockerClient }
import com.spotify.docker.client.DockerClient.BuildParameter
import sbt.Keys._
import sbt._

import scala.util.Try

object SbtDocker extends SbtDocker

trait SbtDocker {

  lazy val dockerTask = Def.task {
    DefaultDockerClient.fromEnv().build()
  }

  def copySourceFilesTask: Def.Initialize[Task[Set[File]]] = Def.task {
    val logger = streams.value.log
    val dst = (buildDirectory in docker).value
    val src = (sourceDirectory in docker).value

    val files = (sourceFiles in docker).value.map { file =>
      (file, dst / IO.relativize(src, file).get)
    }

    if (!dst.exists()) {
      IO.createDirectory(dst)
    }

    val result = IO.copy(files)
    logger.info(result.toString())
    result
  }

  def dockerBuildTask: Def.Initialize[Task[Option[String]]] = Def.task {
    val logger = streams.value.log
    val workDir = (buildDirectory in docker).value.toPath
    Try {
      val result = dockerTask.value.build(workDir, (name in docker).value, new ProgressHandler() {
        override def progress(progressMessage: ProgressMessage): Unit = {
          logger.info(progressMessage.stream())
        }
      }, BuildParameter.NO_CACHE)
      logger.info(s"imageId = $result")
      Some(result)
    }.recover {
      case ex: com.spotify.docker.client.DockerException =>
        logger.error(ex.toString)
        None
    }.get
  }

}