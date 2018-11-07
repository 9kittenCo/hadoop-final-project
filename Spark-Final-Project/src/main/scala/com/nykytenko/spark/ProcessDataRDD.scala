package com.nykytenko.spark
import cats.effect.Effect
import com.nykytenko.config.HdfsConfig
import com.nykytenko.spark.FunctionsRDD._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, _}

class EtlRddDescription(
                      sourceRDD: RDD[Array[String]],
                      transform: RDD[Array[String]] => DataFrame,
                      write: DataFrame => Unit
                    ) extends EtlDescription {
  override def process[F[_]]()(implicit E: Effect[F]): F[Unit] = E.delay {
    write(transform(sourceRDD))
  }
}

class ProcessDataRDD()(implicit ss: SparkSession, config: HdfsConfig) extends ProcessData {

  override val mapEtl: Map[String, EtlRddDescription] = Map[String, EtlRddDescription](
    "_1" -> new EtlRddDescription(sourceRDD = extract(), transform = model51(), write = Writer.DataframeToDb("step51")),
            "_2" -> new EtlRddDescription(sourceRDD = extract(), transform = model52(), write = Writer.DataframeToDb("step52")),
            "_3" -> new EtlRddDescription(sourceRDD = extract(), transform = model6(),  write = Writer.DataframeToDb("step6"))
  )

  def model51()(rdd: RDD[Array[String]]): DataFrame = top10categories(rdd)

  def model52()(rdd: RDD[Array[String]]): DataFrame = top10ProductsByCategory(rdd)

  def model6()(rdd: RDD[Array[String]]): DataFrame = topCountries(rdd)

}
