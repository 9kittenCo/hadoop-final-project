import sbt.Keys._

object Settings {
  val common = Seq(
    name         := "Spark-Final-Project",
    organization := "com.nykytenko",
    version      := "0.0.2-SNAPSHOT",
    scalaVersion := "2.12.7"
  )
}
