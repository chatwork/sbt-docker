import com.chatwork.sbt.docker.BuildOptions._

name := "simple"

name in docker := "test"

sourceFiles in docker := {
  val src = (sourceDirectory in docker).value
  Seq(
    src / "Dockerfile",
    src / "bin" / "date.sh"
  )
}

buildOptions in docker := Set(ForceRm)

libraryDependencies ++= Seq(
)