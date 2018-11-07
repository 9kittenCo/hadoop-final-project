package com.nykytenko

import org.apache.commons.net.util.SubnetUtils
import org.apache.hadoop.hive.ql.exec.{UDF, UDFArgumentLengthException}
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, PrimitiveObjectInspector}
import org.apache.hadoop.io.{BooleanWritable, Text}

class IpToGeo extends UDF {
//  class IpToGeo extends GenericUDF {
//  var converters = Array.empty[ObjectInspectorConverters.Converter]

//  override def initialize(arguments: Array[ObjectInspector]): ObjectInspector = {
//    if (arguments.length != 2) {
//      throw new UDFArgumentLengthException("IpToGeo accepts exactly two arguments.")
//    }
//
//    converters = arguments.map { arg =>
//      ObjectInspectorConverters.getConverter(arg, PrimitiveObjectInspectorFactory.javaStringObjectInspector)
//    }
//
//    PrimitiveObjectInspectorFactory.javaBooleanObjectInspector //(PrimitiveObjectInspector.PrimitiveCategory.BOOLEAN)
//  }

//  override def evaluate(arguments: Array[DeferredObject]): BooleanWritable = {
  def evaluate(ip: Text, subnet: Text): Boolean = {
//    val ip: String     = converters(0).convert(arguments(0).get()).toString
//    val subnet: String = converters(1).convert(arguments(1).get()).toString
    val address = ip.toString
    val cidr    = subnet.toString
//    new BooleanWritable(isInRange(address, cidr))
    isInRange(address, cidr)
  }

//  override def getDisplayString(children: Array[String]): String = s"country(${children(0)}, ${children(1)})"

  def isInRange(ip: String, subnet: String):Boolean = {
    if (ip.nonEmpty || subnet.nonEmpty) {
      val subnetUtils = new SubnetUtils(subnet)
      subnetUtils.getInfo.isInRange(ip)
    } else false

  }
}
