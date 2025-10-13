//package br.com.galsystem.construction.finance.service.images;
//
//import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
//import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.*;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PhotoServiceV2Impl implements PhotoServiceV2 {
//
//    private final S3Client s3Client;
//    private final S3Presigner s3Presigner;
//
//    @Value("${interserver.s3.bucket}")
//    private String bucketName;
//
//    @Override
//    public List<PhotoDTO> store(List<MultipartFile> files) {
//        LocalDate now = LocalDate.now();
//        String year = String.valueOf(now.getYear());
//        String month = String.format("%02d", now.getMonthValue());
//
//        List<PhotoDTO> photos = new ArrayList<>();
//
//        for (MultipartFile file : files) {
//            try {
//                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//                String key = "images/" + year + "/" + month + "/" + filename;
//
//                // Upload para S3
//                s3Client.putObject(
//                        PutObjectRequest.builder()
//                                .bucket(bucketName)
//                                .key(key)
//                                .contentType(file.getContentType())
//                                .build(),
//                        RequestBody.fromInputStream(file.getInputStream(), file.getSize())
//                );
//
//                // Gera URL assinada temporária
//                String presignedUrl = generatePresignedUrl(key);
//
//                photos.add(new PhotoDTO(
//                        filename,
//                        presignedUrl,
//                        LocalDateTime.now(),
//                        getFileExtension(filename),
//                        file.getContentType()
//                ));
//
//            } catch (IOException e) {
//                throw new RuntimeException("Erro ao salvar foto no Object Storage", e);
//            }
//        }
//
//        return photos;
//    }
//
//    @Override
//    public List<Integer> listYears() {
//        ListObjectsV2Response response = s3Client.listObjectsV2(
//                ListObjectsV2Request.builder()
//                        .bucket(bucketName)
//                        .prefix("images/")
//                        .delimiter("/")
//                        .build()
//        );
//
//        return response.commonPrefixes().stream()
//                .map(CommonPrefix::prefix)
//                .map(p -> p.replace("images/", "").replace("/", ""))
//                .filter(p -> !p.isBlank())
//                .map(Integer::parseInt)
//                .sorted(Comparator.reverseOrder())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Integer> listMonths(int year) {
//        ListObjectsV2Response response = s3Client.listObjectsV2(
//                ListObjectsV2Request.builder()
//                        .bucket(bucketName)
//                        .prefix("images/" + year + "/")
//                        .delimiter("/")
//                        .build()
//        );
//
//        return response.commonPrefixes().stream()
//                .map(CommonPrefix::prefix)
//                .map(p -> p.replace("images/" + year + "/", "").replace("/", ""))
//                .filter(p -> !p.isBlank())
//                .map(Integer::parseInt)
//                .sorted()
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PhotoDTO> listPhotos(int year, int month) {
//        String prefix = "images/" + year + "/" + String.format("%02d", month) + "/";
//        ListObjectsV2Response response = s3Client.listObjectsV2(
//                ListObjectsV2Request.builder()
//                        .bucket(bucketName)
//                        .prefix(prefix)
//                        .build()
//        );
//
//        return response.contents().stream()
//                .map(obj -> new PhotoDTO(
//                        obj.key().substring(obj.key().lastIndexOf("/") + 1),
//                        generatePresignedUrl(obj.key()),
//                        obj.lastModified(),
//                        getFileExtension(obj.key()),
//                        null
//                ))
//                .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<PhotoDTO> getAll() {
//        ListObjectsV2Response response = s3Client.listObjectsV2(
//                ListObjectsV2Request.builder()
//                        .bucket(bucketName)
//                        .prefix("images/")
//                        .build()
//        );
//
//        return response.contents().stream()
//                .map(obj -> new PhotoDTO(
//                        obj.key().substring(obj.key().lastIndexOf("/") + 1),
//                        generatePresignedUrl(obj.key()),
//                        obj.lastModified(),
//                        getFileExtension(obj.key()),
//                        null
//                ))
//                .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public PhotoDTO delete(int year, int month, String filename) {
//        String key = "images/" + year + "/" + String.format("%02d", month) + "/" + filename;
//
//        try {
//            HeadObjectResponse head = s3Client.headObject(
//                    HeadObjectRequest.builder()
//                            .bucket(bucketName)
//                            .key(key)
//                            .build()
//            );
//
//            s3Client.deleteObject(DeleteObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(key)
//                    .build()
//            );
//
//            return new PhotoDTO(
//                    filename,
//                    generatePresignedUrl(key),
//                    head.lastModified(),
//                    getFileExtension(filename),
//                    head.contentType()
//            );
//
//        } catch (NoSuchKeyException e) {
//            throw new ResourceNotFoundException("Arquivo não encontrado: " + filename);
//        }
//    }
//
//    private String getFileExtension(String filename) {
//        int dotIndex = filename.lastIndexOf('.');
//        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
//            return filename.substring(dotIndex + 1).toLowerCase();
//        }
//        return "";
//    }
//
//    private String generatePresignedUrl(String key) {
//        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(r -> r
//                .getObjectRequest(GetObjectRequest.builder()
//                        .bucket(bucketName)
//                        .key(key)
//                        .build())
//                .signatureDuration(Duration.ofMinutes(15)));
//
//        return presigned.url().toString();
//    }
//}
