package com.nykytenko

import org.apache.commons.net.util.SubnetUtils
import org.apache.hadoop.hive.ql.exec.{UDF, UDFArgumentLengthException}
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorConverters, PrimitiveObjectInspector}
import org.apache.hadoop.io.{BooleanWritable, Text}

class IpToGeo extends UDF {
  def evaluate(ip: Text, subnet: Text): Boolean = {
    val address = ip.toString
    val cidr    = subnet.toString
    isInRange(address, cidr)
  }
  
  def isInRange(ip: String, subnet: String):Boolean = {
    if (ip.nonEmpty || subnet.nonEmpty) {
      val subnetUtils = new SubnetUtils(subnet)
      subnetUtils.getInfo.isInRange(ip)
    } else false

  }
}
