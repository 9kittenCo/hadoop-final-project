import sbt.Keys._

object Settings {
  val common = Seq(
    name         := "spark-final-project",
    organization := "com.nykytenko",
    version      := "0.0.2-SNAPSHOT",
    scalaVersion := "2.11.12"
  )
}
