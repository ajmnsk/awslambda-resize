package org.aj.awslambda.resize

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Rotation

/**
  * Created by ajlnx on 7/22/16.
  */
trait ImageResizer {

  /**
    * Method re-sizes image
    *
    * @param srcImage original image data
    * @param orientation original image orientation
    * @param maxSize target max X size to adjust if required
    * @return resized image data
    */
  def resize(srcImage: BufferedImage, orientation: Int, maxSize: Int): BufferedImage = {

    //fix orienation
    val tmpImage = orientation match {
      case 1 => srcImage
      case 2 => // Flip X
        Scalr.rotate(srcImage, Rotation.FLIP_HORZ)
      case 3 => // PI rotation
        Scalr.rotate(srcImage, Rotation.CW_180)
      case 4 => // Flip Y
        Scalr.rotate(srcImage, Rotation.FLIP_VERT)
      case 5 => // - PI/2 and Flip X
        val tmpImage1 = Scalr.rotate(srcImage, Rotation.CW_90)
        Scalr.rotate(tmpImage1, Rotation.FLIP_HORZ)
      case 6 => // -PI/2 and -width
        Scalr.rotate(srcImage, Rotation.CW_90)
      case 7 => // PI/2 and Flip
        val tmpImage1 = Scalr.rotate(srcImage, Rotation.CW_90)
        Scalr.rotate(tmpImage1, Rotation.FLIP_VERT);
      case 8 => // PI / 2
        Scalr.rotate(srcImage, Rotation.CW_270)
      case _ => srcImage
    }
    //end of orientation fix

    //return image data
    if (tmpImage.getWidth > maxSize) Scalr.resize(tmpImage, maxSize) else tmpImage
  }

  /**
    * Method returns image orientation value, defaults to '1' if not able to look up,
    *
    * @param byteArrayInputStream image data
    * @return image orientation value
    *
    */
  def getOrientation(byteArrayInputStream: ByteArrayInputStream): Int = {

    //get metadata to find orientation
    val metadata = ImageMetadataReader.readMetadata(byteArrayInputStream)

    //get image orientation
    val directory = metadata.getFirstDirectoryOfType(classOf[ExifIFD0Directory])
    directory.isEmpty match {
      case true => 1 //default value
      case false => {
        //keep looking up the value
        directory.containsTag(274) match {
          case false => 1 //did not find a tag for orientation, so default to 1
          case true => {
            //Description:Right side, top (Rotate 90 CW), DirectoryName:ExifIFD0Directory, TagName:Orientation, TagType:274
            directory.getInteger(274)
          }
        }
      }
    }
  }

}
