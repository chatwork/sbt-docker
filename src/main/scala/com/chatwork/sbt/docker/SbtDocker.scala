package com.chatwork.sbt.docker

import com.chatwork.sbt.docker.SbtDockerKeys._
import com.spotify.docker.client.messages.{ AuthConfig, ProgressMessage }
import com.spotify.docker.client.{ ProgressHandler, DefaultDockerClient }
import com.spotify.docker.client.DockerClient.BuildParameter
import sbt.Keys._
import sbt._
import Keys._
import scala.util.Try

object SbtDocker extends SbtDocker

trait SbtDocker {

  lazy val dockerClient = Def.task {
    if ((login in docker).value) {
      val e = (emailAddress in docker).value
      val p = (password in docker).value
      val u = (userName in docker).value
      val authConfig = AuthConfig.builder().username(u).email(e).password(p).build()
      DefaultDockerClient.fromEnv().authConfig(authConfig).build()
    } else {
      DefaultDockerClient.fromEnv().build()
    }
  }

  def generateDockerfileTask: Def.Initialize[Task[File]] = Def.task {
    val logger = streams.value.log
    val c = (templateContext in docker).value
    val t = (dockerfileTemplate in docker).value
    val df = (dockerfile in docker).value
    if (t.exists()) {
      logger.info("generate docker file from template file.")
      new DockerfileFreemakerBuilder(t.getParentFile, t.base, c, df).build.get
      logger.info("generated docker file from template file.")
      df
    } else {
      df
    }
  }

  def cleanSourceFilesTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = streams.value.log
    val dst = (buildDirectory in docker).value
    logger.info(s"delete $dst")
    IO.delete(dst)
    logger.info(s"deleted $dst")
  }

  def copySourceFilesTask: Def.Initialize[Task[Set[File]]] = Def.task {
    val logger = streams.value.log
    val dst = (buildDirectory in docker).value
    val src = (sourceDirectory in docker).value

    val files = ((sourceFiles in docker).value :+ generateDockerfileTask.value).map { file =>
      (file, dst / IO.relativize(src, file).get)
    }

    if (!dst.exists()) {
      IO.createDirectory(dst)
    }

    val result = IO.copy(files)
    logger.info(result.toString())
    result
  }

  def toBuildParameter(bo: BuildOptions.Value): BuildParameter = {
    bo match {
      case BuildOptions.Quiet   => BuildParameter.QUIET
      case BuildOptions.NoCache => BuildParameter.NO_CACHE
      case BuildOptions.NoRm    => BuildParameter.NO_RM
      case BuildOptions.ForceRm => BuildParameter.FORCE_RM
    }
  }

  def dockerBuildTask: Def.Initialize[Task[Option[String]]] = Def.task {
    val logger = streams.value.log
    val workDir = (buildDirectory in docker).value.toPath
    val repositoryName = (name in docker).value
    val bo = (buildOptions in docker).value.map(toBuildParameter)
    Try {
      val result = dockerClient.value.build(workDir, repositoryName, new ProgressHandler() {
        override def progress(progressMessage: ProgressMessage): Unit = {
          logger.info(progressMessage.stream())
        }
      }, bo.toArray: _*)
      logger.info(s"imageId = $result")
      Some(result)
    }.recover {
      case ex: com.spotify.docker.client.DockerException =>
        logger.error(ex.toString)
        None
    }.get
  }

}