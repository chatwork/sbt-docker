import com.chatwork.sbt.docker.BuildOptions._

name := "simple"

name in docker := "j5ik2o/test"

sourceFiles in docker := {
  val src = (sourceDirectory in docker).value
  Seq(
    src / "Dockerfile",
    src / "bin" / "echo.sh"
  )
}

buildOptions in docker := Set(ForceRm)

libraryDependencies ++= Seq(
)