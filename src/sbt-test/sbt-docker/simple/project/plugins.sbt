logLevel := Level.Warn

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/"
)

{
  val projectVersion = sys.props("project.version")
  if(projectVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                 |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("com.chatwork" % "sbt-docker" % projectVersion)
}