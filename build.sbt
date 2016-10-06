
lazy val root = (project in file(".")).
  settings(
    name := "awslambda-resize",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.11.8",
    organization := "org.aj",
    retrieveManaged := true
  )

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.typesafe" % "config" % "1.3.0",
  "com.drewnoakes" % "metadata-extractor" % "2.9.1",
  "org.imgscalr" % "imgscalr-lib" % "4.2"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*)  => MergeStrategy.discard
  case x                              => MergeStrategy.first
}