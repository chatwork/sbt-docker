package com.chatwork.sbt.docker


import java.util.Date

import com.fasterxml.jackson.databind.util.StdDateFormat

class DockerDateFormat extends StdDateFormat {
  override def parse(source: String): Date = {
    val _source = if (source.matches(".+\\.\\d{9}Z$"))
      source.replaceAll("\\d{6}Z$", "Z")
    else
      source

    super.parse(_source)
  }
}
