package org.aj.awslambda

import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URLDecoder

import com.amazonaws.services.lambda.runtime.events.S3Event
import java.util.regex.Pattern

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Implementation for AWS Lambda to resize original image and upload it to s3
  * There is a list of target 'X' sizes to adjust, 'Y' size will be adjusted proportionally
  * If original image size is smaller than target 'X', image size is left as is.
  *
  */
class ImageHandler extends S3 with Source with Processor {

  import Settings._

  /**
    * Method called on s3 bucket put event.
    * Event information is used to get source file
    * And execute re-size logic
    *
    * @param event Put event information
    */
  def resize(event: S3Event): Unit = {

    try {

      //get source file information
      val s3 = event.getRecords().get(0).getS3
      val srcBucket = s3.getBucket().getName()
      val srcKey = decodeS3Key(s3.getObject().getKey())
      val dstBucket = destBucket

      println(s"\nSource bucket: ${srcBucket}, key: ${srcKey}; dest bucket: ${dstBucket}\n")

      //get image type from file extension
      getImageType(srcKey) match {
        case Some(imageType) => {

          val future = process(srcBucket, srcKey, imageType, dstBucket).map { f => f
          } recover {
            case error => throw new RuntimeException(error.getMessage)
          }

          //wait to finish
          val urls = Await.result(future, timeOutMs)
          println(urls)

        }
        case _ => None
      }

      //return void (Unit)
      ()

    } catch {

      case e: IOException => throw new RuntimeException(e)
      case e: Throwable => throw new RuntimeException(e)
      case e: RuntimeException => throw e

    }
  }

  /**
    * Method to get image type based of file extension.
    * Throws exception is unexpected extension value
    *
    * @param srcKey s3 key [path] for image file
    * @return image file extension if able to extract from the key
    */
  private def getImageType(srcKey: String): Option[String] = {
    val matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(srcKey)
    matcher.matches() match {
      case true => {
        //get source image type and validate
        val imageType = matcher.group(1)
        imageTypes.get(imageType) match {
          case Some(_) => Some(imageType)
          case _ => None
        }
      }
      case _ => None
    }
  }

  private def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  private def process(srcBucket: String, srcKey: String, imageType: String, dstBucket: String): Future[List[Url]] = {

    //get source image with it's sizes
    val sourceImage: (BufferedImage, Url) = getImage(srcBucket, srcKey)
    //re-size
    val original: Future[Url] = process(sourceImage._1, sourceImage._2, imageType, dstBucket, srcKey)
    val resized: List[Option[Future[Url]]] = sizes.map { size =>
      if (sourceImage._2.width != size) Some(process(sourceImage._1, size, imageType, dstBucket, srcKey))
      else None
    }
    //return
    Future.sequence(original :: resized.flatten)

  }

}
