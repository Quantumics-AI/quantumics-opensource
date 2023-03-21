package com.qs.api;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class AwsCustomConfiguration {

    @Value("${s3.credentials.accessKey}")
    private String s3AccessKey;

    @Value("${s3.credentials.secretKey}")
    private String s3SecretKey;

    @Value("${athena.credentials.accessKey}")
    private String athenaAccessKey;

    @Value("${athena.credentials.secretKey}")
    private String athenSecretKey;

    @Bean
    public AmazonAthena awsAthenaClient() {
        final BasicAWSCredentials awsAthenaaCredentials =
                new BasicAWSCredentials(athenaAccessKey, athenSecretKey);
        final AmazonAthena athenaClient =
                AmazonAthenaClient.builder()
                        .withRegion(Regions.EU_WEST_2)
                        .withCredentials(new AWSStaticCredentialsProvider(awsAthenaaCredentials))
                        .build();

        return athenaClient;
    }

    @Bean
    public AmazonS3 awsS3Client() {
        final BasicAWSCredentials awsS3Credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.EU_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(awsS3Credentials))
                .build();
    }

    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(5242880);
        return multipartResolver;
    }
}
