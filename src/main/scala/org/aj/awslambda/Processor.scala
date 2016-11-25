package org.aj.awslambda

import java.awt.image.BufferedImage
import scala.concurrent.Future

/**
  * Trait to re-size and upload image
  */
trait Processor extends S3 with Resizer with Key with Uploader {

  import scala.concurrent.ExecutionContext.Implicits._

  def process(image: BufferedImage, width: Int, imageType: String, bucket: String, key: String): Future[Url] = Future {
    val resizedImage: (BufferedImage, Url) = resize(image, width)
    val newKey: String = create(key, resizedImage._2)
    val uploadedUrl: String = upload(resizedImage._1, imageType, bucket, newKey)
    resizedImage._2.copy(url = Some(uploadedUrl))
  }

  def process(image: BufferedImage, url: Url, imageType: String, bucket: String, key: String): Future[Url] = Future {
    val newKey: String = create(key, url)
    val uploadedUrl: String = upload(image, imageType, bucket, newKey)
    url.copy(url = Some(uploadedUrl))
  }

}