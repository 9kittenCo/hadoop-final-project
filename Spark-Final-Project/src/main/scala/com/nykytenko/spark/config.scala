package com.nykytenko

import cats.effect.Effect
import pureconfig.error.ConfigReaderException
import cats.implicits._
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

package object config {

  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  case class SparkConfig(name: String, master: String)

  case class HdfsConfig(
                         options: Map[String, String],
                         prefixPath: String,
                         eventsPath: String,
                         blocksPath: String,
                         countriesPath: String,
                         mySqlPath: String
                       )

  case class Config(spark: SparkConfig, hdfs: HdfsConfig)

  import pureconfig._

  def load[F[_]](implicit E: Effect[F]): F[Config] = E.delay {
    loadConfig[Config]
  }.flatMap {
    case Right(config) => E.pure(config)
    case Left(e) => E.raiseError(new ConfigReaderException[Config](e))
  }
}
