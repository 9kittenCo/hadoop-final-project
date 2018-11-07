package com.nykytenko.spark

import com.nykytenko.config.HdfsConfig
import com.opencsv.CSVParser
import org.apache.commons.net.util.SubnetUtils
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}

case class TopCategoryProducts(
                           category: String,
                           product: String,
                           count: Int
                         )

object FunctionsRDD {

  import scala.collection.JavaConverters.seqAsJavaListConverter

  def extract()(implicit ss: SparkSession, config: HdfsConfig): RDD[Array[String]] = {
    val sc = ss.sparkContext

    sc.textFile(config.prefixPath + config.eventsPath).map(parse)
  }

  private def parse(value: String): Array[String] = {
    val csvParser = new CSVParser()

    if (value.nonEmpty) {
      csvParser.parseLine(value)
    }
    Array.empty[String]
  }

  def top10categories(rdd: RDD[Array[String]])(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {

    val purchaseSchema = StructType(
      List(
        StructField("category", DataTypes.StringType),
        StructField("price", DataTypes.DoubleType)
      )
    )

    val resultArray = rdd.map(line => (line(3), line(1).toDouble))
      .keyBy(_._1)
      .mapValues(_._2)
      .reduceByKey(_ + _)
      .sortBy(_._2, ascending = false)
      .take(10)

    val result = resultArray.map(a => Row(a._1, a._2)).toList.asJava

    ss.createDataFrame(result, purchaseSchema)
  }

  def top10ProductsByCategory(rdd: RDD[Array[String]])(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {
    val schema = StructType(
      List(
        StructField("category", DataTypes.StringType),
        StructField("product", DataTypes.StringType),
        StructField("price", DataTypes.DoubleType)
      )
    )

    val byCategoryAndProduct: RDD[((String, String), Int)] = rdd.map(p => ((p(3), p(0)), 1)).reduceByKey(_ + _)
    val resultRdd = byCategoryAndProduct
      .map(p => TopCategoryProducts(p._1._1, p._1._2, p._2)) //category, product, count
      .keyBy(p => p.category)
      .aggregateByKey(List.empty[TopCategoryProducts])(
        (l, r) => (l :+ r).sortBy(v => - v.count).take(10),
        (l1, l2) => (l1 ++ l2).sortBy(- _.count).take(10))
      .values.flatMap(list => list.iterator)

    val result = resultRdd.map(r => Row(r.category, r.product, r.count))
    ss.createDataFrame(result, schema)
  }

  def topCountries(rdd: RDD[Array[String]])(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {

    val sc = ss.sparkContext

    val schema = StructType(
      List(
        StructField("country", DataTypes.StringType),
        StructField("price", DataTypes.DoubleType)
      )
    )

    val blocksRDD = sc.textFile(config.prefixPath + config.blocksPath)
    val header1 = blocksRDD.first
    val blocks = blocksRDD.filter(_ != header1).map(parse)
      .filter(l => !l(0).isEmpty && !l(1).isEmpty).map(i => (i(0).toString, i(1).toLong)).keyBy(_._2)

    val countriesRDD = sc.textFile(config.prefixPath + config.countriesPath)
    val header2 = countriesRDD.first
    val countries = countriesRDD.filter(_ != header2).map(parse)
      .filter(l => !l(0).isEmpty && !l(5).isEmpty).map(i => (i(0).toLong, i(5).toString)).keyBy(_._1)
    countries.partitionBy(new HashPartitioner(blocks.partitions.length))


    val countryBlocks = countries.join(blocks).map(j => (j._2._1._2, j._2._2._1)) //(country, network)

    val purchaseCollection = rdd.map(p => (p(4).toString, p(1).toLong)) //ip, price

    val resultArray: Array[(String, Long)] = purchaseCollection
      .cartesian(countryBlocks)
      .filter { p =>
        val ip = p._1._1 //ip, price
      val subnet = p._2._2 //(country, network)
        if (ip.nonEmpty || subnet.nonEmpty) {
          val subnetUtils = new SubnetUtils(subnet)
          subnetUtils.getInfo.isInRange(ip)
        } else false
      }.map(pair => (pair._2._1, pair._1._2)) //(country, price)
      .keyBy(_._1) //(country, (country, price))
      .mapValues(_._2)
      .reduceByKey(_ + _)
      .sortBy(_._2, ascending = false)
      .take(10)

    val result = resultArray.map(a => Row(a._1, a._2)).toList.asJava

    ss.createDataFrame(result, schema)
  }

}
