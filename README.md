# sbt-docker

[![Build Status](https://travis-ci.org/chatwork/sbt-docker.svg)](https://travis-ci.org/chatwork/sbt-docker)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-docker_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.chatwork/sbt-docker_2.10)
[![Scaladoc](http://javadoc-badge.appspot.com/com.chatwork/sbt-docker.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.chatwork/sbt-docker_2.10)
[![Reference Status](https://www.versioneye.com/java/com.chatwork:sbt-docker_2.10/reference_badge.svg?style=flat)](https://www.versioneye.com/java/com.chatwork:sbt-docker_2.10/references)

## Installation

Add the following to your `project/plugins.sbt` (Scala 2.10.x, and Scala 2.11.x):

### Release Version

```scala
resolvers += "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.chatwork" % "sbt-docker" % "1.0.2")
```

### Snapshot Version

```scala
resolvers += "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.chatwork" % "sbt-docker" % "1.0.3-SNAPSHOT")
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

If you want to set `--force-rm=true` option, `BuildOptions` will be as follow. 

```scala
BuildOptions in docker := Seq(ForceRm)
```

#### Start

```sh
$ sbt
> docker::start
info] Set(/Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/target/docker/Dockerfile, /Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/target/docker/bin/date.sh)
[info] Step 0 : FROM busybox
[info]  ---> 8c2e06607696
[info] Step 1 : ADD bin/date.sh /
[info]  ---> 17611e6f5cd4
[info] Removing intermediate container 38cc4a3c1eac
[info] Step 2 : CMD sh /date.sh
[info]  ---> Running in 9f5aa473c4f8
[info]  ---> 8fdbc30fb900
[info] Removing intermediate container 9f5aa473c4f8
[info] Successfully built 8fdbc30fb900
[info] imageId = 8fdbc30fb900
[success] Total time: 2 s, completed 2015/06/01 10:42:58
[info] containerId = 9b2640f23f2a8c58fa59a7216ce5eeb49b801d06e4097149433bf63827e3dec6, out = Mon Jun 1 01:42:58 UTC 2015
```

#### StartAndWait

```sh
$ sbt docker::startAndWait
[info] Loading project definition from /Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/project
[info] Set current project to simple (in build file:/Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/)
[info] Set(/Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/target/docker/Dockerfile, /Users/j5ik2o/sbt-docker/src/sbt-test/sbt-docker/simple/target/docker/bin/date.sh)
[info] Step 0 : FROM busybox
[info]  ---> 8c2e06607696
[info] Step 1 : ADD bin/date.sh /
[info]  ---> Using cache
[info]  ---> 17611e6f5cd4
[info] Step 2 : CMD sh /date.sh
[info]  ---> Using cache
[info]  ---> 8fdbc30fb900
[info] Successfully built 8fdbc30fb900
[info] imageId = 8fdbc30fb900
[info] containerId = 0c5f25eead653ea59c22fcfd9b93a1702d78f509ec22db5a727491344932164d, out = Mon Jun 1 01:43:58 UTC 2015
[success] Total time: 2 s, completed 2015/06/01 10:43:58
```

#### Push/Pull

```sh
$ sbt docker::push
<snip>
[info] status = The push refers to a repository [j5ik2o/test] (len: 1)
[info] id = 5ed02c7da45e, status = Buffering to Disk
[info] id = 5ed02c7da45e, status = Pushing
[info] id = 5ed02c7da45e, status = Pushing
[info] id = 5ed02c7da45e, status = Pushing
[info] id = 5ed02c7da45e, status = Image successfully pushed
[info] id = 5ed02c7da45e, status = Image already exists
[info] id = 4ddab5d60536, status = Buffering to Disk
[info] id = 4ddab5d60536, status = Pushing
[info] id = 4ddab5d60536, status = Pushing
[info] id = 4ddab5d60536, status = Image successfully pushed
[info] id = 8c2e06607696, status = Image already exists
[info] id = 6ce2e90b0bc7, status = Image already exists
[info] id = cf2616975b4a, status = Image already exists
[info] status = Digest: sha256:3bc3b648e03564b87e8962bfc7b2c1e2c3a58a75a5c8bdbd2dd1c5293087094d
```

```sh
$ sbt docker::pull
[info] status = Pulling repository j5ik2o/test
[info] id = 0ebb5ffaa96c, status = Pulling image (latest) from j5ik2o/test
[info] id = 0ebb5ffaa96c, status = Pulling image (latest) from j5ik2o/test, endpoint: https://registry-1.docker.io/v1/
[info] id = 0ebb5ffaa96c, status = Pulling dependent layers
[info] id = cf2616975b4a, status = Download complete
[info] id = 6ce2e90b0bc7, status = Download complete
[info] id = 8c2e06607696, status = Download complete
[info] id = f18371e6f7ab, status = Download complete
[info] id = 0ebb5ffaa96c, status = Download complete
[info] id = 0ebb5ffaa96c, status = Download complete
[info] status = Status: Image is up to date for j5ik2o/test
```




### Dockerfile template support by Freemarker

- Put Dockefile.ftl to source directory.

```
 + project-root directory
   + docker
     + Dockerfile.ftl
     + etc
```

- Set context values to build.sbt as follows.

```scala
templateContext in docker := Map(
    "name" -> (name in thisProjectRef).value,
    "version" -> (version in thisProjectRef).value
)
```

- Set reference to context values into Dockerfile.ftl 

Dockerfile.ftl

```
FROM busybox
ADD bin/echo.sh /
CMD ["sh", "/echo.sh", "${name}-${version}"]
```

echo.sh

```
#!/bin/sh
echo $1
```
