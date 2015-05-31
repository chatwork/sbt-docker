package com.chatwork.sbt.docker

import java.io.FileWriter

import sbt._

import scala.collection.JavaConverters._
import scala.util.Try

trait DockerfileBuilder {

  def build: Try[Unit]

}

class DockerfileFreemakerBuilder(base: File, templateName: String, context: Map[String, String], dest: File) extends DockerfileBuilder {

  private val cfg = new freemarker.template.Configuration
  cfg.setDirectoryForTemplateLoading(base)

  override def build: Try[Unit] = Try {
    var writer: FileWriter = null
    try {
      val template = cfg.getTemplate(templateName)
      writer = new FileWriter(dest)
      template.process(context.asJava, writer)
    } finally {
      if (writer != null)
        writer.close()
    }
  }
}

