package org.aj.awslambda.resize

import com.typesafe.config.ConfigFactory

/**
  * Created by ajlnx on 7/11/16.
  */
object Destination {

    private val config = ConfigFactory.load("application")

    val bucketPrefix: String = config.getString("destination.bucketPrefix")
    val maxSizes: List[String] = config.getString("destination.maxSizes").split(',').toList
}
