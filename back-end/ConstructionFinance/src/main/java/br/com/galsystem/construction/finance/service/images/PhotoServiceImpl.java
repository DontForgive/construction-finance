package br.com.galsystem.construction.finance.service.images;
import br.com.galsystem.construction.finance.dto.images.PhotoDTO;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    @Value("${app.files.storage-root}")
    private String storageRoot;

    @Override
    public List<PhotoDTO> store(List<MultipartFile> files) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        Path dir = Paths.get(storageRoot, "images", year, month);

        List<PhotoDTO> photos = new ArrayList<>();

        try {
            Files.createDirectories(dir);

            for (MultipartFile file : files) {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = dir.resolve(filename);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                FileTime fileTime = Files.getLastModifiedTime(path);
                LocalDateTime uploadedAt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());

                String fileName = path.getFileName().toString();
                String fileType = "";
                String mimeType = Files.probeContentType(path);

                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    fileType = fileName.substring(dotIndex + 1).toLowerCase();
                }

                photos.add(new PhotoDTO(
                        filename,
                        "/files/images/" + year + "/" + month + "/" + filename,
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
    public List<PhotoDTO> listPhotos(int year, int month) {
        Path dir = Paths.get(storageRoot, "images", String.valueOf(year), String.format("%02d", month));
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(path -> {
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
                                    path.getFileName().toString(),"/files/images/" + year + "/" + String.format("%02d", month) + "/" + path.getFileName(),
                                    uploadedAt, fileType, mimeType
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar fotos", e);
        }
    }

    @Override
    public List<PhotoDTO> getAll() {
        Path dir = Paths.get(storageRoot, "images");
        if (!Files.exists(dir)) return List.of();

        try (Stream<Path> stream = Files.walk(dir, 3)) {
            return stream.filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            String month = path.getParent().getFileName().toString();
                            String year = path.getParent().getParent().getFileName().toString();
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
                                    path.getFileName().toString(),
                                    "/files/images/" + year + "/" + month + "/" + path.getFileName(),
                                    uploadedAt, fileType, mimeType
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparing(PhotoDTO::getUploadedAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar todas as fotos", e);
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

            return new PhotoDTO(
                    fileName,
                    "/files/images/" + y + "/" + m + "/" + fileName,
                    uploadedAt,
                    fileType,
                    mimeType
            );
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir foto: " + filename, e);
        }
    }

}
