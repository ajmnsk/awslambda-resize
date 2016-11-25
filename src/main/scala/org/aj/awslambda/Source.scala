package org.aj.awslambda

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.util.IOUtils
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Rotation

/**
  * Trait to read data from s3, and fix / get orientation
  *
  */
trait Source {
  self: S3 =>

  /**
    * Method returns Image data with it's sizes
    *
    * @param bucket image bucket
    * @param key image key
    * @return image data with it's sizes
    */
  def getImage(bucket: String, key: String): (BufferedImage, Url) = {

    val getObjectRequest = new GetObjectRequest(bucket, key)
    val s3Object = s3Client.getObject(getObjectRequest)
    val bytes =  IOUtils.toByteArray(s3Object.getObjectContent())

    //get image orientation
    val orientation: Int = getOrientation(bytes)
    //read the source image
    val image: BufferedImage = getImage(bytes)

    fix(image, orientation)
  }

  private def getImage(bytes: Array[Byte]): BufferedImage = ImageIO.read(new ByteArrayInputStream(bytes))

  /**
    * Method to fix orientation if required
    *
    * @param image original image data
    * @param orientation original image orientation
    * @return image data with width, height sizes
    */
  private def fix(image: BufferedImage, orientation: Int): (BufferedImage, Url) = {

    //fix orienation
    val tmpImage = orientation match {
      case 1 => image
      case 2 => // Flip X
        Scalr.rotate(image, Rotation.FLIP_HORZ)
      case 3 => // PI rotation
        Scalr.rotate(image, Rotation.CW_180)
      case 4 => // Flip Y
        Scalr.rotate(image, Rotation.FLIP_VERT)
      case 5 => // - PI/2 and Flip X
        val tmpImage1 = Scalr.rotate(image, Rotation.CW_90)
        Scalr.rotate(tmpImage1, Rotation.FLIP_HORZ)
      case 6 => // -PI/2 and -width
        Scalr.rotate(image, Rotation.CW_90)
      case 7 => // PI/2 and Flip
        val tmpImage1 = Scalr.rotate(image, Rotation.CW_90)
        Scalr.rotate(tmpImage1, Rotation.FLIP_VERT);
      case 8 => // PI / 2
        Scalr.rotate(image, Rotation.CW_270)
      case _ => image
    }
    //end of orientation fix

    //return image data
    (tmpImage, Url(tmpImage.getWidth(), tmpImage.getHeight()))
  }

  /**
    * Method returns image orientation value, defaults to '1' if not able to look up,
    *
    * @param bytes image data
    * @return image orientation value
    *
    */
  private def getOrientation(bytes: Array[Byte]): Int = {

    val byteArrayInputStream: ByteArrayInputStream = new ByteArrayInputStream(bytes)

    //get metadata to find orientation
    val metadata = ImageMetadataReader.readMetadata(byteArrayInputStream)

    /*
    println("")
    for(d <- metadata.getDirectories().asScala) {
      println(d)
      for(t <- d.getTags().asScala)
      { println(s" ${t}, TagName:${t.getTagName}, TagType:${t.getTagType}") }
    }
    println("")
    */

    //get image orientation
    val directory = metadata.getFirstDirectoryOfType(classOf[ExifIFD0Directory])
    if (directory != null)
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
    else 1

  }
}