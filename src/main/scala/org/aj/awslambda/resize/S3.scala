package org.aj.awslambda.resize

import java.io.InputStream
import java.net.URLDecoder

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}

/**
  * Created by ajlnx on 6/23/16.
  */
trait S3 {

  //get s3 client
  val s3Client: AmazonS3 = new AmazonS3Client()

  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  /**
    * Method uploads image to s3 location
    *
    * @param bucket s3 bucket to upload image into
    * @param key s3 key [path] for image
    * @param size s3 image file size
    * @param contentType image type is available
    * @param content image itself as bytes stream
    * @return s3 path to the uploaded image
    */
  def upload(bucket: String, key: String, size: Long, contentType: Option[String], content: InputStream): String = {

    //build meta object to describe the data
    val meta: ObjectMetadata = new ObjectMetadata()
    meta.setContentLength(size)

    //set content type if available
    contentType match {
      case Some(v) => meta.setContentType(v)
      case _ => None
    }

    //store data to s3
    s3Client.putObject(bucket, key, content, meta)

    //get URL for just stored object
    val url = s3Client.getUrl(bucket, key)

    url.toString
  }

}

object S3 {
  val JPG_TYPE = "jpg"
  val JPG_MIME = "image/jpeg"
  val PNG_TYPE = "png"
  val PNG_MIME = "image/png"
}
