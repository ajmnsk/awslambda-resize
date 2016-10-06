AWS Lambda to resize s3 uploaded image using scala
=========================================================

## Resizing Event

AWS Lambda function is executed on s3 bucket Put event: ```Event type: ObjectCreatedByPut```.
Original ```X``` size is adjusted to a Target ```X```, and ```Y``` is adjusted proportionally.
There can be multiple ```X``` Target values, which are provided via application configuration file.

## Resized image naming

Resized image(s) are stored in a different s3 bucket named as: ```original_bucket-resized```.
Original image s3 key is not changed, except file name.
File name is adjusted by appending resized image ```width``` and ```height``` values.
Here is a sample naming for Target X ```1040```.
File name is changed by appending value ```_1440_810```:

Original image path: ```https://s3-us-west-2.amazonaws.com/images.mycompany.com/boss/20160704/dancing.jpg```
Resized image path: ```https://s3-us-west-2.amazonaws.com/images.mycompany.com-resized/boss/20160704/dancing_1440_810.jpg```

## Usage

Go to project folder, and build the library:
```
$ activator assembly
```
Once built, ```./target/scala-2.11/awslambda-resize-assembly-0.0.1-SNAPSHOT.jar``` becomes available for usage.
[Here](https://aws.amazon.com/blogs/compute/writing-aws-lambda-functions-in-scala/) is an additional example on how to build, deploy to AWS, and test scala built Lambda.


## Author & license

If you have any questions regarding this project contact:
Andrei <ajmnsk@gmail.com>
For licensing info see LICENSE file in project's root directory.