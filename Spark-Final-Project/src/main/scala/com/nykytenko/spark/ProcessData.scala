package com.nykytenko.spark

import java.sql.Connection
import java.util.Properties
import java.sql.DriverManager
import cats.effect.Effect
import com.nykytenko.config.HdfsConfig
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode}

trait ProcessData{
  val mapEtl: Map[String, EtlDescription]
}

trait EtlDescription {
  def process[F[_]]()(implicit E: Effect[F]): F[Unit]
}

object Writer {
  def DataframeToDb(tableName: String)(df: DataFrame)(implicit config: HdfsConfig): Unit = {
    val props = new Properties()
    props.setProperty("driver", "com.mysql.jdbc.Driver")
    props.setProperty("user", "root")
    df.write.mode(SaveMode.Append).jdbc(config.mySqlPath, tableName, props)
  }

  def RddToDb(tableName: String)(rdd: RDD[Row])(implicit config: HdfsConfig): Unit = {
    val f = tableName match {
      case "step51"   => writeStep51()(_,_)
      case "step52"   => writeStep52()(_,_)
      case "step6"    => writeStep6()(_,_)
    }
    rdd.foreachPartition { iter =>
      val conn: Connection = DriverManager.getConnection(config.mySqlPath)
      f(conn, iter)
      conn.close()
    }
  }

  private def writeStep51()(conn: Connection, i: Iterator[Row]): Unit = {
    val stat = conn.prepareStatement("INSERT INTO step51 (category, purchase_count) VALUES (?,?)")
    for (row <- i) {
      stat.setString(1, row.getString(0))
      stat.setLong(2  , row.getInt(1))

      stat.executeUpdate
    }
  }

  private def writeStep52()(conn: Connection, i: Iterator[Row]): Unit = {
    val stat = conn.prepareStatement("INSERT INTO step52 (category, product, purchase_count) VALUES (?,?,?)")
    for (row <- i) {
      stat.setString(1, row.getString(0))
      stat.setString(2, row.getString(1))
      stat.setLong(3  , row.getInt(2))

      stat.executeUpdate
    }

  }

  private def writeStep6()(conn: Connection, i: Iterator[Row]): Unit = {
    val stat = conn.prepareStatement("INSERT INTO step6 (country, total_money_sum) VALUES (?,?)")
    for (row <- i) {
      stat.setString(1, row.getString(0))
      stat.setLong(2  , row.getInt(1))

      stat.executeUpdate
    }

  }

}