package com.nykytenko

import java.text.SimpleDateFormat
import java.util.Calendar


import scala.util.Random

object Helper {
  def randomDate: String = {

    val formatter: SimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    var cal: Calendar = Calendar.getInstance()
    val str_date1 = "01-01-2018 02:10:15"
    val str_date2 = "07-01-2018 02:10:20"

    cal.setTime(formatter.parse(str_date1))
    val value1 = cal.getTimeInMillis

    cal.setTime(formatter.parse(str_date2))
    val value2 = cal.getTimeInMillis

    val value3 = (value1 + Math.random() * (value2 - value1)).toLong
    cal.setTimeInMillis(value3)

    formatter.format(cal.getTime)
  }

  def randomIP: String = {
    val random = new Random()
    random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256)
  }

  def randint(from: Int, to: Int): Int = {
    val random = new Random()
    from + random.nextInt(to - from + 1)
  }

  def getCsv(length: Int): List[Array[String]] = {

    val random = new Random()

    val productList = List("sneakers", "hat", "macbook", "basketball ball", "spoon", "book", "ring", "jersey", "sneakers", "socks")
    val categoryList = List("wear", "electronics", "men's footwear", "sports", "kitchen", "science", "jewellery")

    val listOfRecords: List[Array[String]] = (0 to length).foldLeft(List.empty[Array[String]]) { (acc, _) =>
      acc :+ Array(
        productList(random.nextInt(productList.length)),
        randint(0, 5000).toString,
        randomDate,
        categoryList(random.nextInt(categoryList.length)),
        randomIP)
    }
    listOfRecords
  }
}
