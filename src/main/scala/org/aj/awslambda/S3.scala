package org.aj.awslambda

import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}

trait S3 {
  val s3Client: AmazonS3 = new AmazonS3Client()
}
