package com.dyrnq.bdcm;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioFileUploaderTest {
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        // TOD
        // https://archive.apache.org/dist/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz
        // http://192.168.66.122:19000/my-bucket/archive.apache.org/dist/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("192.168.66.122", 19000, false)
                            .credentials("vUR3oLMF5ds8gWCP", "odWFIZukYrw9dY0G5ezDKMZWbhU0S4oD")
                            .build();

            // Make 'my-bucket' bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("my-bucket").build());
            if (!found) {
                // Make a new bucket called 'my-bucket'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("my-bucket").build());
            } else {
                System.out.println("Bucket 'my-bucket' already exists.");
            }

            // Upload 'pom.xml' as object name 'pom.xml' to bucket
            // 'asiatrip'.
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("my-bucket")
                            .object("pom.xml")
                            .filename("pom.xml")
                            .build());
            System.out.println("'pom.xml' is successfully uploaded as object 'pom.xml' to bucket 'my-bucket'.");
            System.out.println("http://192.168.66.122:19000/my-bucket/pom.xml");
            System.out.println("http://192.168.66.122:9982/pom.xml");

        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }
}
