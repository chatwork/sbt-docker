# sbt-docker

[![Build Status](https://travis-ci.org/chatwork/sbt-docker.svg)](https://travis-ci.org/chatwork/sbt-docker)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-docker_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-docker_2.11)
[![Scaladoc](http://javadoc-badge.appspot.com/com.chatwork/sbt-docker.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.chatwork/sbt-docker_2.11)
[![Reference Status](https://www.versioneye.com/java/com.chatwork:sbt-docker_2.11/reference_badge.svg?style=flat)](https://www.versioneye.com/java/com.chatwork:sbt-docker_2.11/references)

## Installation

Add the following to your `project/plugin.sbt` (Scala 2.10.x, and Scala 2.11.x):

### Release Version

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-docker" % "1.0.0")
```

### Snapshot Version

```scala
resolvers += "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.chatwork" % "sbt-docker" % "1.0.0-SNAPSHOT")
```

## Usage

### resource files for Docker

- In defaults, put files to `docker` directory in project root.

```
 + project-root directory
   + docker
     + Dockerfile
     + etc
```

### Task

#### Build

```sh
$ sbt docker::build
```

- build options

If you want to set `--form-rm=true` option, `BuildOptions` will be as follow. 

```scala
BuildOptions in docker := Seq(ForceRm)
```
