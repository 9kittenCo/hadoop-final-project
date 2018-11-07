import sbt._

object Dependencies {
  object Version {
    val OpenCsv = "4.3.2"
  }

  val baseDependencies = Seq(
    "com.opencsv"      % "opencsv"                    % Version.OpenCsv
  )
}
