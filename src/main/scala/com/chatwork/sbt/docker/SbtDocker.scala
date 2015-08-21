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
    val logger = streams.value.log
    val e = (emailAddress in docker).value
    val p = (password in docker).value
    val u = (userName in docker).value
    logger.info(s"userName = $u, emailAddress = $e")
    AuthConfig.builder().username(u).email(e).password(p).build()
  }

  lazy val dockerClient = Def.task {
    if ((login in docker).value) {
      DefaultDockerClient.fromEnv().authConfig(authConfig.value)
        .readTimeoutMillis((clientReadTimeoutMillis in docker).value)
        .connectTimeoutMillis((clientConnectTimeoutMillis in docker).value)
        .build()
    } else {
      DefaultDockerClient.fromEnv()
        .readTimeoutMillis((clientReadTimeoutMillis in docker).value)
        .connectTimeoutMillis((clientConnectTimeoutMillis in docker).value)
        .build()
    }
  }

  def progressHandler(logger: Logger)(f: ProgressMessage => Option[String]) = {
    new ProgressHandler() {
      override def progress(progressMessage: ProgressMessage): Unit = {
        if (progressMessage != null) {
          f(progressMessage).foreach(e => logger.info(e))
        }
      }
    }
  }

  def generateDockerfileTask: Def.Initialize[Task[File]] = Def.task {
    val logger = streams.value.log
    val c = (templateContext in docker).value
    val t = (dockerfileTemplate in docker).value
    val df = (dockerfile in docker).value
    if (t.exists()) {
      new DockerfileFreemakerBuilder(t.getParentFile, t.getName, c, df).build.get
      logger.info(s"generated docker file from template file. dockerTemplate = $t, templateContext = $c, dockerfile = $df")
      df
    } else {
      df
    }
  }

  def cleanSourceFilesTask: Def.Initialize[Task[Unit]] = Def.task {
    val logger = streams.value.log
    val dst = (buildDirectory in docker).value
    IO.delete(dst)
    logger.info(s"deleted $dst")
  }

  def copySourceFilesTask: Def.Initialize[Task[Set[File]]] = Def.task {
    val logger = streams.value.log
    val dst = (buildDirectory in docker).value
    val src = (sourceDirectory in docker).value

    val gFile = (generateDockerfile in docker).value

    val files = (sourceFiles in docker).value.map {
      case (file, path) =>
        (file, dst / path)
    } :+ (gFile, dst / IO.relativize(src, gFile).get)

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
      if ((login in docker).value) {
        sut.auth(authConfig.value)
        sut.push(repositoryName, progressHandler(logger) { pm =>
          Some(
            Seq(
              Option(pm.id).map(e => s"id = $e").toSeq,
              Option(pm.status).map(e => s"status = $e").toSeq,
              Option(pm.error()).map(e => s"error = $e").toSeq
            ).flatten.mkString(", ")
          )
        })
      } else {
        sut.push(repositoryName, progressHandler(logger) { pm =>
          Some(
            Seq(
              Option(pm.id).map(e => s"id = $e").toSeq,
              Option(pm.status).map(e => s"status = $e").toSeq,
              Option(pm.error()).map(e => s"error = $e").toSeq
            ).flatten.mkString(", ")
          )
        })
      }
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
        sut.pull(repositoryName, authConfig.value, progressHandler(logger) { pm =>
          Some(
            Seq(
              Option(pm.id).map(e => s"id = $e").toSeq,
              Option(pm.status).map(e => s"status = $e").toSeq,
              Option(pm.error()).map(e => s"error = $e").toSeq
            ).flatten.mkString(", ")
          )
        })
        logger.info(s"docker pull $repositoryName")
      } else {
        sut.pull(repositoryName, progressHandler(logger) { pm =>
          Some(
            Seq(
              Option(pm.id).map(e => s"id = $e").toSeq,
              Option(pm.status).map(e => s"status = $e").toSeq,
              Option(pm.error()).map(e => s"error = $e").toSeq
            ).flatten.mkString(", ")
          )
        })
        logger.info(s"docker pull $repositoryName")
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
      logger.info(s"docker start, containerId = ${containerCreation.id}")
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
      val result = sut.build(workDir, repositoryName, progressHandler(logger) { pm => Some(pm.stream()) }, bo.toArray: _*)
      logger.info(s"docker build, imageId = $result")
      Some(result)
    }.recover {
      case ex: DockerException =>
        logger.error(ex.toString)
        None
    }.get
  }

}