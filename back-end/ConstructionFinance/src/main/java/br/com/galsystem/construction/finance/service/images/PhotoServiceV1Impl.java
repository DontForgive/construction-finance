package br.com.galsystem.construction.finance.service.images;

import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class PhotoServiceV1Impl implements PhotoServiceV1 {

    @Value("${app.files.storage-root}")
    private String storageRoot;

    @Override
    public List<PhotoDTO> store(List<MultipartFile> files) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        Path dir = Paths.get(storageRoot, "images", year, month);
        Path thumbDir = Paths.get(storageRoot, "thumbnails", year, month);

        List<PhotoDTO> photos = new ArrayList<>();

        try {
            Files.createDirectories(dir);
            Files.createDirectories(thumbDir);

            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
                String filename = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;
                Path path = dir.resolve(filename);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Gerar Thumbnail
                Path thumbPath = thumbDir.resolve(filename);
                try {
                    net.coobird.thumbnailator.Thumbnails.of(path.toFile())
                            .size(300, 300)
                            .outputQuality(0.8)
                            .toFile(thumbPath.toFile());
                } catch (Throwable e) {
                    // Se falhar ao gerar thumbnail (ex: não é imagem), copia o original ou ignora erro
                    Files.copy(path, thumbPath, StandardCopyOption.REPLACE_EXISTING);
                }

                FileTime fileTime = Files.getLastModifiedTime(path);
                LocalDateTime uploadedAt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());

                String fileType = "";
                String mimeType = Files.probeContentType(path);

                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                    fileType = filename.substring(dotIndex + 1).toLowerCase();
                }

                photos.add(new PhotoDTO(
                        filename,
                        "/files/images/" + year + "/" + month + "/" + filename,
                        "/files/thumbnails/" + year + "/" + month + "/" + filename,
                        uploadedAt,
                        fileType,
                        mimeType
                ));
            }

            return photos;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar fotos", e);
        }
    }

    @Override
    public List<Integer> listYears() {
        Path dir = Paths.get(storageRoot, "images");
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isDirectory)
                    .map(path -> Integer.parseInt(path.getFileName().toString()))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar anos", e);
        }
    }

    @Override
    public List<Integer> listMonths(int year) {
        Path dir = Paths.get(storageRoot, "images", String.valueOf(year));
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isDirectory)
                    .map(path -> Integer.parseInt(path.getFileName().toString()))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar meses", e);
        }
    }

    @Override
    public Page<PhotoDTO> listPhotos(int year, int month, Pageable pageable) {
        String y = String.valueOf(year);
        String m = String.format("%02d", month);
        Path dir = Paths.get(storageRoot, "images", y, m);
        if (!Files.exists(dir)) return Page.empty(pageable);

        try (Stream<Path> stream = Files.list(dir)) {
            List<PhotoDTO> allPhotos = stream.filter(Files::isRegularFile)
                    .map(path -> mapToDTO(path, y, m))
                    .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
                    .collect(Collectors.toList());

            return getPage(allPhotos, pageable);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar fotos", e);
        }
    }

    @Override
    public Page<PhotoDTO> getAll(Pageable pageable) {
        Path dir = Paths.get(storageRoot, "images");
        if (!Files.exists(dir)) return Page.empty(pageable);

        try (Stream<Path> stream = Files.walk(dir, 3)) {
            List<PhotoDTO> allPhotos = stream.filter(Files::isRegularFile)
                    .map(path -> {
                        String m = path.getParent().getFileName().toString();
                        String y = path.getParent().getParent().getFileName().toString();
                        return mapToDTO(path, y, m);
                    })
                    .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
                    .collect(Collectors.toList());

            return getPage(allPhotos, pageable);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar todas as fotos", e);
        }
    }

    private PhotoDTO mapToDTO(Path path, String year, String month) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            LocalDateTime uploadedAt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            String fileName = path.getFileName().toString();
            String fileType = "";
            String mimeType = Files.probeContentType(path);

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileType = fileName.substring(dotIndex + 1).toLowerCase();
            }

            return new PhotoDTO(
                    fileName,
                    "/files/images/" + year + "/" + month + "/" + fileName,
                    "/files/thumbnails/" + year + "/" + month + "/" + fileName,
                    uploadedAt, fileType, mimeType
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Page<T> getPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if (start > list.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    @Async
    @Override
    public void generateMissingThumbnails() {
        System.out.println("[INFO] Iniciando processamento de miniaturas em segundo plano...");
        Path imagesDir = Paths.get(storageRoot, "images");
        if (!Files.exists(imagesDir)) {
            System.err.println("[ERROR] Diretório de imagens não encontrado: " + imagesDir);
            return;
        }

        try (Stream<Path> stream = Files.walk(imagesDir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(imagePath -> {
                        try {
                            // Extrair ano/mês/nome do path original
                            // images/yyyy/MM/filename
                            Path relativePath = imagesDir.relativize(imagePath);
                            Path thumbPath = Paths.get(storageRoot, "thumbnails").resolve(relativePath);

                            if (!Files.exists(thumbPath)) {
                                Files.createDirectories(thumbPath.getParent());
                                try {
                                    System.out.println("[INFO] Gerando thumbnail para: " + relativePath);
                                    // Chamada direta ao Thumbnailator
                                    net.coobird.thumbnailator.Thumbnails.of(imagePath.toFile())
                                            .size(300, 300)
                                            .outputQuality(0.8)
                                            .toFile(thumbPath.toFile());
                                } catch (Throwable e) {
                                    // Se não for imagem ou erro no processamento, apenas copia
                                    System.err.println("[WARN] Falha ao processar com Thumbnailator (Error/Exception): " + e.getMessage());
                                    Files.copy(imagePath, thumbPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        } catch (Exception e) {
                            // Log de erro silencioso para não interromper o loop
                            System.err.println("[ERROR] Erro ao processar thumbnail para " + imagePath + ": " + e.getMessage());
                        }
                    });
            System.out.println("[INFO] Processamento de miniaturas concluído com sucesso.");
        } catch (IOException e) {
            System.err.println("[ERROR] Erro crítico ao percorrer diretório de imagens: " + e.getMessage());
        }
    }

    @Override
    public PhotoDTO delete(int year, int month, String filename) {
        String y = String.valueOf(year);
        String m = String.format("%02d", month);

        Path baseDir = Paths.get(storageRoot, "images", y, m);
        Path target = baseDir.resolve(filename).normalize();

        if (!target.startsWith(baseDir)) {
            throw new SecurityException("Caminho de arquivo inválido");
        }

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new ResourceNotFoundException("Arquivo não encontrado: " + filename);
        }

        try {
            // Coleta metadados antes de apagar
            FileTime fileTime = Files.getLastModifiedTime(target);
            LocalDateTime uploadedAt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            String fileName = target.getFileName().toString();
            String fileType = "";
            String mimeType = Files.probeContentType(target);

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileType = fileName.substring(dotIndex + 1).toLowerCase();
            }

            Files.delete(target);

            // Tenta deletar thumbnail também
            Path thumbTarget = Paths.get(storageRoot, "thumbnails", y, m, filename);
            Files.deleteIfExists(thumbTarget);

            return new PhotoDTO(
                    fileName,
                    "/files/images/" + y + "/" + m + "/" + fileName,
                    "/files/thumbnails/" + y + "/" + m + "/" + fileName,
                    uploadedAt,
                    fileType,
                    mimeType
            );
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir foto: " + filename, e);
        }
    }

}
