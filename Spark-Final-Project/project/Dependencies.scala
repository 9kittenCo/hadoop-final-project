import sbt._

object Dependencies {
  object Version {
    val PureConfig = "0.9.1"
    val Cats       = "1.0.1"
    val CatsEffect = "0.10"
    val Spark      = "2.4.0"
    val Opencsv    = "4.3.2"
    val Slf4j      = "1.7.25"
  }

  val baseDependencies = Seq(
    "com.opencsv"           % "opencsv"      % Version.Opencsv,
    "com.github.pureconfig" %% "pureconfig"  % Version.PureConfig,

    "org.typelevel"         %% "cats-effect" % Version.CatsEffect,

    "org.apache.spark"      %% "spark-core"  % Version.Spark,
    "org.apache.spark"      %% "spark-sql"   % Version.Spark
  )
}
