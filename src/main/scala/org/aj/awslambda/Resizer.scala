package org.aj.awslambda

import java.awt.image.BufferedImage
import org.imgscalr.Scalr

/**
  * Created by ajlnx on 7/22/16.
  */
trait Resizer {

  /**
    * Method to re-size image
    *
    * @param image image data
    * @param size target max X size to adjust if required
    * @return resized image and it's sizes
    */
  def resize(image: BufferedImage, size: Int): (BufferedImage, Url) = {
    val img = Scalr.resize(image, size)
    (img, Url(img.getWidth(), img.getHeight()))
  }
}