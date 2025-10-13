//package br.com.galsystem.construction.finance.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//
//import java.net.URI;
//
//@Configuration
//public class S3Config {
//
//    @Value("${interserver.s3.access-key}")
//    private String accessKey;
//
//    @Value("${interserver.s3.secret-key}")
//    private String secretKey;
//
//    @Value("${interserver.s3.region}")
//    private String region;
//
//    @Value("${interserver.s3.endpoint}")
//    private String endpoint;
//
//    @Bean
//    public S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(region))
//                .endpointOverride(URI.create(endpoint)) // precisa porque Interserver não é AWS
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//    }
//
//    @Bean
//    public S3Presigner s3Presigner() {
//        return S3Presigner.builder()
//                .region(Region.of(region))
//                .endpointOverride(URI.create(endpoint))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//    }
//}
