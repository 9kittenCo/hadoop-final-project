package com.nykytenko.spark

import com.nykytenko.config.HdfsConfig
import org.apache.commons.net.util.SubnetUtils
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DataTypes, DoubleType, StructField, StructType}

object FunctionsDS {
  def extract()(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {
    val purchaseSchema = StructType(
      List(
        StructField("productCategory" , DataTypes.StringType),
        StructField("productName"     , DataTypes.StringType),
        StructField("productPrice"    , DataTypes.DoubleType),
        StructField("purchaseDateTime", DataTypes.TimestampType),
        StructField("clientIp"        , DataTypes.StringType)
      )
    )

    ss
      .read
      .schema(purchaseSchema)
      .format("csv")
      .options(config.options)
      .csv(config.prefixPath + config.eventsPath)

  }

  def top10categories(df: DataFrame)(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {
    import ss.implicits._

    val eventsDS = df
      .toDF("name", "price", "purchaseDateTime", "category", "ip")
      .withColumn("price", col("price").cast(DoubleType))
      .as[Event]

    eventsDS.createOrReplaceTempView("purchases")

    eventsDS.cache()
    ss.sql(
      "select category, count(*) as purchase_count from purchases group by category order by purchase_count desc limit 10"
    )
  }

  def top10ProductsByCategory(df: DataFrame)(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {

    ss.sql(
      "SELECT productCategory, productName, cnt " +
        "FROM ( " +
        " SELECT productCategory, productName, cnt, ROW_NUMBER() OVER (PARTITION BY productCategory ORDER BY cnt DESC) rank " +
        " FROM ( " +
        "   SELECT productCategory, productName, count(*) cnt " +
        "   FROM purchase " +
        "   GROUP BY productCategory, productName" +
        " ) grouped" +
        ") ranked " +
        "WHERE rank <= 10"
    )
  }

  def topCountries(df: DataFrame)(implicit ss: SparkSession, config: HdfsConfig): DataFrame = {
    import ss.implicits._

    val networksDF = ss.read
      .option("header"      , "true")
      .option("inferSchema" , "true")
      .csv(config.prefixPath + "GeoLite2-Country-Blocks-IPv4.csv")

    val countriesDF = ss.read
      .option("header"      , "true")
      .option("inferSchema" , "true")
      .csv(config.prefixPath + "GeoLite2-Country-Locations-en.csv")

    val countryNetworkDS: Dataset[CountryNetwork] = countriesDF.join(networksDF, "geoname_id")
      .select($"country_name".alias("country"), $"network")
      .as[CountryNetwork]

    countryNetworkDS.createOrReplaceTempView("countryNetwork")

    countryNetworkDS.cache()

    ss.udf.register("is_in_range",
      (ip: String, network: String) => new SubnetUtils(network).getInfo.isInRange(ip))

    ss.sql(
      "SELECT cn.country, round(sum(p.price), 2) moneySpent " +
        "FROM purchase p JOIN countryNetwork cn ON is_in_range(p.ip, cn.network) " +
        "GROUP BY country " +
        "ORDER BY moneySpent DESC " +
        "LIMIT 10"
    )
  }


}
