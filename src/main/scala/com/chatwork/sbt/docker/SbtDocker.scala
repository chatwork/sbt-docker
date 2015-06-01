package com.chatwork.sbt.docker

import java.text.SimpleDateFormat

import com.chatwork.sbt.docker.SbtDockerKeys._
import com.google.common.base.Charsets
import com.spotify.docker.client.DockerClient.{ AttachParameter, BuildParameter, ListImagesParam }
import com.spotify.docker.client._
import com.spotify.docker.client.messages.{ AuthConfig, ContainerConfig, ProgressMessage }
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Try

object SbtDocker extends SbtDocker

trait SbtDocker {

  lazy val authConfig = Def.task {
    val e = (emailAddress in docker).value
    val p = (password in docker).value
    val u = (userName in docker).value
    AuthConfig.builder().username(u).email(e).password(p).build()
  }

  lazy val dockerClient = Def.task {
    if ((login in docker).value) {
      DefaultDockerClient.fromEnv().authConfig(authConfig.value).build()
    } else {
      DefaultDockerClient.fromEnv().build()
    }
  }

  lazy val progressHandler = Def.task {
    val logger = streams.value.log
    new ProgressHandler() {
      override def progress(progressMessage: ProgressMessage): Unit = {
        logger.info(progressMessage.stream())
      }
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

  def dockerPushTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = streams.value.log
    val sut = dockerClient.value
    val repositoryName = (name in docker).value
    Try {
      sut.push(repositoryName, progressHandler.value)
    }.recover {
      case ex: DockerException =>
        logger.error(ex.toString)
    }.get
  }

  def dockerPullTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = streams.value.log
    val sut = dockerClient.value
    val repositoryName = (name in docker).value
    Try {
      if ((login in docker).value) {
        sut.pull(repositoryName, authConfig.value, progressHandler.value)
      } else {
        sut.pull(repositoryName, progressHandler.value)
      }
    }.recover {
      case ex: DockerException =>
        logger.error(ex.toString)
    }.get
  }

  def dockerListImagesTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = streams.value.log
    val sut = dockerClient.value
    val result = sut.listImages(ListImagesParam.allImages())
    val sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    val df = new DockerDateFormat()
    result.asScala.foreach { image =>
      val id = image.id()
      val created = image.created()
      val ts = df.parse(created)
      val timestamp = sdf.format(ts)
      val repoTags = image.repoTags()
      val size = image.size()
      val virtualSize = image.virtualSize()
      logger.info("%s, %10s, %10d, %10d, %s".format(id, repoTags.asScala.mkString(":"), size, virtualSize, timestamp))
    }
  }

  def dockerStartTask: Def.Initialize[Task[Option[Future[String]]]] = Def.task {
    val logger = streams.value.log
    val sut = dockerClient.value
    dockerBuildTask.value.map { imageId =>
      val config = ContainerConfig.builder().image(imageId).build()
      val containerCreation = sut.createContainer(config)
      sut.startContainer(containerCreation.id)
      Future {
        var logStream: LogStream = null
        try {
          logStream = sut.attachContainer(
            containerCreation.id,
            AttachParameter.LOGS,
            AttachParameter.STDOUT,
            AttachParameter.STDERR,
            AttachParameter.STREAM
          )
          while (logStream.hasNext) {
            val logMessage = logStream.next
            val content = Charsets.UTF_8.decode(logMessage.content())
            logger.info(s"containerId = ${containerCreation.id}, out = ${content}")
          }
        } finally {
          if (logStream != null)
            logStream.close()
        }
        containerCreation.id
      }
    }
  }

  def dockerStartAndWaitTask: Def.Initialize[Task[Unit]] = Def.task {
    val sut = dockerClient.value
    dockerStartTask.value.map { future =>
      val id = Await.result(future, Duration.Inf)
      sut.waitContainer(id)
    }
  }

  def dockerBuildTask: Def.Initialize[Task[Option[String]]] = Def.task {
    val logger = streams.value.log
    val sut = dockerClient.value
    val workDir = (buildDirectory in docker).value.toPath
    val repositoryName = (name in docker).value
    val bo = (buildOptions in docker).value.map(toBuildParameter)
    Try {
      val result = sut.build(workDir, repositoryName, progressHandler.value, bo.toArray: _*)
      logger.info(s"imageId = $result")
      Some(result)
    }.recover {
      case ex: DockerException =>
        logger.error(ex.toString)
        None
    }.get
  }

}