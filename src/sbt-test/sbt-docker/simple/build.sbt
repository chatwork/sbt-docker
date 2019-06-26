import com.chatwork.sbt.docker.BuildOptions._

name := "simple"

name in docker := "j5ik2o/test"

login in docker := true

emailAddress in docker := "j5ik2o@gmail.com"

userName in docker := "j5ik2o"

sourceFiles in docker := {
  val src = (sourceDirectory in docker).value
  Seq(
    (src / "Dockerfile",  "Dockerfile"),
    (src / "bin" / "echo.sh", "bin/echo.sh")
  )
}

buildOptions in docker := Set(ForceRm)

libraryDependencies ++= Seq(
)