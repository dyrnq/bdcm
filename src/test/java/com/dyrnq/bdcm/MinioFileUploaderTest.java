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

    static final String accessKey = "vUR3oLMF5ds8gWCP";
    static final String secretKey = "odWFIZukYrw9dY0G5ezDKMZWbhU0S4oD";
    static final String bucket = "my-bucket";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        // TOD
        // https://archive.apache.org/dist/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz
        // http://192.168.66.122:19000/my-bucket/archive.apache.org/dist/hive/hive-4.0.1/apache-hive-4.0.1-bin.tar.gz
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("192.168.66.122", 19000, false)
                    .credentials(accessKey, secretKey)
                    .build();

            // Make 'my-bucket' bucket if not exist.
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                // Make a new bucket called 'my-bucket'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            } else {
                System.out.println("Bucket '" + bucket + "' already exists.");
            }

            // Upload 'pom.xml' as object name 'pom.xml' to bucket 'my-bucket'.
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket)
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
