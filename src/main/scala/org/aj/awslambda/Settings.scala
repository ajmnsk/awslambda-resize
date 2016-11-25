package org.aj.awslambda

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
  * Created by ajlnx on 7/11/16.
  */
object Settings {

    private val config = ConfigFactory.load("application")

    val destBucket: String = config.getString("destination.bucket")
    //use Set to remove duplicates if any
    val sizes: List[Int] = config.getString("destination.sizes").split(',').toList.map(size => size.toInt).toSet.toList
    val delimiter: String = config.getString("destination.delimiter")
    val imageTypes: Map[String, String] = config.getString("destination.imageTypes").split(';').toList.map {item =>
      val pair = item.split(':').toList
      (pair(0), pair(1))
    }.toMap
    val timeOutMs: Duration = config.getString("timeOutMs").toInt.millis
}
