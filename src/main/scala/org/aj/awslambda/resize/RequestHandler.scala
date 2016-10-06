package org.aj.awslambda.resize

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}

import com.amazonaws.services.lambda.runtime.events.S3Event
import java.util.regex.Pattern
import javax.imageio.ImageIO

import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.util.IOUtils
import scala.collection.mutable.ListBuffer

/**
  * Implementation for AWS Lambda to resize original image and upload it to s3
  * There is a list of target 'X' sizes to adjust.
  * 'Y' size will be adjusted proportionally.
  * If original image size is smaller than target 'X', image size will be left as is.
  *
  */
class RequestHandler extends S3 with ImageResizer {

  import Destination._
  import S3._

  /**
    * Resize image using Scalr library
    * Method also sets proper orientation if required
    *
    * @param resizedImage image data
    * @param imageType image file extension
    * @param bucket image s3 destination bucket
    * @param key image s3 key [path]
    * @param keys List to collect keys of uploaded images
    */
  private def upload(resizedImage: BufferedImage, imageType: String, bucket: String, key: String,
                     keys: ListBuffer[String], urls: ListBuffer[String]): Unit = {

    val newKey = createKey(key, resizedImage.getWidth, resizedImage.getHeight)

    //upload file if not done yet for this key
    if (!keys.contains(newKey)) {

      //re-encode image to target format
      val os = new ByteArrayOutputStream()
      ImageIO.write(resizedImage, imageType, os)
      val is = new ByteArrayInputStream(os.toByteArray())
      val contentType: Option[String] =
        if (JPG_TYPE == imageType) Some(JPG_MIME)
        else if (PNG_TYPE == imageType) Some(PNG_MIME)
        else None

      //upload
      val url = upload(bucket, newKey, os.size(), contentType, is)
      urls += url
      keys += newKey

    }

  }

  /**
    * Method to get image file extension
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
        if (!(imageType == JPG_TYPE) && !(imageType == PNG_TYPE)) {
          None
        } else {
          Some(imageType)
        }
      }
      case _ => None
    }
  }

  /**
    * Method adjusts existing s3 key by appending image width and height values to the file name
    *
    * @param srcKey original s3 key
    * @param width image width
    * @param height image height
    * @return new key value
    */
  private def createKey(srcKey: String, width: Int, height: Int): String = {
    //split key
    val splitted = srcKey.split('/')

    //get file name
    val fileName = splitted(splitted.length - 1)

    //split file name
    val splittedFileName = fileName.split('.')
    if (splittedFileName.length < 2)
      throw new RuntimeException(s"Invalid file name: ${fileName}. It should have extension.")

    //compose new file name
    splittedFileName(splittedFileName.length - 2) = splittedFileName(splittedFileName.length - 2) + "_" + width.toString + "_" + height.toString

    //assign new file name
    splitted(splitted.length - 1) = splittedFileName.mkString(".")

    //return new key
    splitted.mkString("/")
  }

  /**
    * Method called on s3 bucket put event.
    * Event information is used to get source file
    * And execute re-size logic
    *
    * @param event Put event information
    */
  def resizeImage(event: S3Event): Unit = {

    try {

      //get source file information
      val s3 = event.getRecords().get(0).getS3
      val srcBucket = s3.getBucket().getName()
      val srcKey = decodeS3Key(s3.getObject().getKey())
      val dstBucket = srcBucket + "-" + bucketPrefix

      println(s"\nSource: bucket: ${srcBucket}, key: ${srcKey}\n")

      getImageType(srcKey) match {

        case Some(imageType) => {

          //download an image into a stream
          val getObjectRequest = new GetObjectRequest(srcBucket, srcKey)
          val s3Object = s3Client.getObject(getObjectRequest)
          val bytes =  IOUtils.toByteArray(s3Object.getObjectContent())

          //get image orientation
          val orientation = getOrientation( new ByteArrayInputStream(bytes) )

          //read the source image
          val srcImage = ImageIO.read( new ByteArrayInputStream(bytes) )

          //needed to keep track of uploaded re-sized images
          var keys = new ListBuffer[String]()
          var urls = new ListBuffer[String]()

          //execute resize logic
          maxSizes foreach {maxSize =>
            val resizedImage = resize(srcImage, orientation, maxSize.toInt)
            upload(resizedImage, imageType, dstBucket, srcKey, keys, urls)
          }

          println(s"Resized images: ${urls}")

        }
        case _ => None
      }
      ()

    } catch {
      case e: IOException => {
        throw new RuntimeException(e)
      }
      case e: RuntimeException => {
        throw e
      }
    }

  }
}
