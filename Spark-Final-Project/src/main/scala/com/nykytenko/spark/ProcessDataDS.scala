package com.nykytenko.spark

import java.sql.Timestamp

import cats.effect.Effect
import com.nykytenko.config.HdfsConfig
import com.nykytenko.spark.FunctionsDS._
import org.apache.spark.sql.{DataFrame, _}

case class Event(name: String, price: Double, purchaseDateTime: Timestamp, category: String, ip: String)

case class CountryNetwork(country: String, network: String)

class EtlDsDescription(
                        sourceDF: DataFrame,
                        transform: DataFrame => DataFrame,
                        write: DataFrame => Unit
                      ) extends EtlDescription {
  override def process[F[_]]()(implicit E: Effect[F]): F[Unit] = E.delay {
    write(sourceDF.transform(transform))
  }
}

class ProcessDataDS()(implicit sparkSession: SparkSession, config: HdfsConfig) extends ProcessData {

  val mapEtl: Map[String, EtlDsDescription] = Map[String, EtlDsDescription](
    "_1" -> new EtlDsDescription(sourceDF = extract(), transform = model51(), write = Writer.DataframeToDb("step51")),
            "_2" -> new EtlDsDescription(sourceDF = extract(), transform = model52(), write = Writer.DataframeToDb("step52")),
            "_3" -> new EtlDsDescription(sourceDF = extract(), transform = model6(),  write = Writer.DataframeToDb("step6"))
  )

  def model51()(df: DataFrame): DataFrame = df transform top10categories

  def model52()(df: DataFrame): DataFrame = df transform top10ProductsByCategory

  def model6()(df: DataFrame): DataFrame = df transform topCountries
}
