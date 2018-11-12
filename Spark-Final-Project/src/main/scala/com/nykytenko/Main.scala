package com.nykytenko

import cats.effect.{Effect, IO}
import cats.implicits._
import com.nykytenko.spark.{EtlDescription, ProcessDataDS, ProcessDataRDD, Session}
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

sealed trait InterfaceType
case object Rdd extends InterfaceType
case object Ds extends InterfaceType

object Main {

  val logger: Logger = LoggerFactory.getLogger(Main.getClass.getName)

  case class EtlResult(value: EtlDescription, session: SparkSession)

  def main(args: Array[String]): Unit = {

    val interfaceTypeList = List("rdd", "ds")
    val mIT = Map[String, InterfaceType]("rdd" -> Rdd, "ds" -> Ds)

    if (args.length != 2) logger.error("Wrong number of parameters!")
    else if (interfaceTypeList.contains(args(0).toLowerCase.trim)) {
      val iType = mIT(args(0).toLowerCase.trim)
      val name = args(1).toLowerCase.trim

      program[IO](iType, name).unsafeRunSync()
    } else logger.error("Wrong processing type")
  }

  def program[F[_]](interfaceType: InterfaceType, etlName: String)(implicit E: Effect[F]): F[Unit] =
    for {
      logic <- mainLogic[F](interfaceType, etlName)
      _     <- logic.value.process()
      _     <- Session[F].close(logic.session)
    } yield ()

  def mainLogic[F[_]](interfaceType: InterfaceType, name:String)(implicit E: Effect[F]): F[EtlResult] =
    for {
      configuration <- config.load[F]
      session       <- new Session[F].createFromConfig(configuration.spark)
      processData   = interfaceType match {
        case Ds  => new ProcessDataDS()(session, configuration.hdfs)
        case Rdd => new ProcessDataRDD()(session, configuration.hdfs)
      }
    } yield EtlResult(processData.mapEtl(name), session)
}


