name := "simple"

name in docker := "test"

sourceFiles in docker := {
  val src = (sourceDirectory in docker).value
  Seq(
    src / "Dockerfile",
    src / "bin" / "date.sh"
  )
}

libraryDependencies ++= Seq(
)