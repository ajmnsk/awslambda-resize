package org.aj.awslambda

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import javax.imageio.ImageIO
import com.amazonaws.services.s3.model.ObjectMetadata

/**
  * Created by ajlnx on 11/18/16.
  */
trait Uploader {
  self: S3 =>

  import Settings._

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
  private def upload(bucket: String, key: String, size: Long, contentType: Option[String], content: InputStream): String = {

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

  /**
    * Resize image using Scalr library
    * Method also sets proper orientation if required
    *
    * @param image image data
    * @param imageType image file extansion
    * @param bucket image s3 destination bucket
    * @param key image s3 key [path]
    */
  def upload(image: BufferedImage, imageType: String, bucket: String, key: String): String = {

    //re-encode image to target format
    val os = new ByteArrayOutputStream()
    ImageIO.write(image, imageType, os)

    val is = new ByteArrayInputStream(os.toByteArray())
    val contentType: Option[String] = imageTypes.get(imageType)
    //upload
    val url = upload(bucket, key, os.size(), contentType, is)

    url
  }

}
