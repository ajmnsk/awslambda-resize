package org.aj.awslambda

/**
  * Trait to create a new key value from existing by appending to the name image actual sizes
  */
trait Key {

  import Settings._

  /**
    * Method adjusts existing s3 key by appending image width and height values to the file name
    *
    * @param key original s3 key
    * @param url image width, and height
    * @return new key value
    */
  def create(key: String, url: Url): String = {
    //split key
    val splitted = key.split('/')

    //get file name
    val fileName = splitted(splitted.length - 1)

    //split file name
    val splittedFileName = fileName.split('.')
    if (splittedFileName.length < 2)
      throw new RuntimeException(s"Invalid file name: ${fileName}, extension missed.")

    //compose new file name
    splittedFileName(splittedFileName.length - 2) = splittedFileName(splittedFileName.length - 2) + delimiter +
      url.width.toString + delimiter +
      url.height.toString

    //assign new file name
    splitted(splitted.length - 1) = splittedFileName.mkString(".")

    //return new key
    splitted.mkString("/")
  }

}
