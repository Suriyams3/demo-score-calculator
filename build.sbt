// build.sbt
name := "CustomerEngagementScorer"

version := "1.0"

scalaVersion := "2.12.17" // AWS Glue 4.0 supports Scala 2.12

// This setting creates a fat JAR by packaging all dependencies (except Spark and Scala)
// which is necessary for AWS Glue jobs that have external dependencies like Sttp.
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// Spark is marked as 'provided' because AWS Glue already provides it.
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.4.1" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.4.1" % "provided",

  // Sttp is a modern, easy-to-use HTTP client for Scala.
  // We'll use the core library and the Java client backend.
  "com.softwaremill.sttp.client3" %% "core" % "3.8.3",
  "com.softwaremill.sttp.client3" %% "okhttp-backend" % "3.8.3" // Or 'apache-backend', 'akka-backend' etc.
)