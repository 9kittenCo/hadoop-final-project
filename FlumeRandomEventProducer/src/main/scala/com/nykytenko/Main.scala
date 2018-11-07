package com.nykytenko

import java.io._
import java.net.Socket

import com.opencsv.CSVWriter

object Main {
  def main(args: Array[String]): Unit = {

    //get socket port
    val port = args.headOption.getOrElse("27049").toInt

    val socket: Socket                 = new Socket("localhost", port)
    val bufferedWriter: BufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
    val csvWriter: CSVWriter           = new CSVWriter(bufferedWriter)
    println("sending!....")

    import scala.collection.JavaConverters._

    csvWriter.writeAll(Helper.getCsv(200).asJava)

    bufferedWriter.flush()
    csvWriter.close()
    socket.close()

    println("....done!")
  }
}