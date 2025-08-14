package br.com.galsystem.construction.finance.service.file;

import br.com.galsystem.construction.finance.files.UploadArea;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import br.com.galsystem.construction.finance.exception.ConflictException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.files.storage-root}")
    private String storageRoot;

    @Value("${app.files.public-base-url}")
    private String publicBaseUrl; // ex.: /files

    // Extensões permitidas por área (ajuste conforme a sua regra)
    private static final Map<UploadArea, Set<String>> ALLOWED_EXT = Map.of(
            UploadArea.EXPENSES, Set.of("pdf","png","jpg","jpeg","webp"),
            UploadArea.SUPPLIERS, Set.of("pdf","png","jpg","jpeg","webp"),
            UploadArea.CATEGORIES, Set.of("png","jpg","jpeg","webp")
    );

    // Tipos de conteúdo permitidos (básico)
    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    );

    @Override
    public String store(UploadArea area, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("Arquivo vazio");
        }

        // Nome original & extensão
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String ext = StringUtils.getFilenameExtension(original);
        ext = (ext != null ? ext.toLowerCase(Locale.ROOT) : "");

        // Validação por área
        Set<String> allowed = ALLOWED_EXT.getOrDefault(area, Set.of());
        if (!allowed.contains(ext)) {
            throw new ConflictException("Extensão não permitida: " + ext);
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_TYPES.contains(contentType)) {
            throw new ConflictException("Tipo de conteúdo não permitido: " + contentType);
        }

        // yyyy/MM/dd
        LocalDate today = LocalDate.now();
        String y = String.valueOf(today.getYear());
        String m = String.format("%02d", today.getMonthValue());
        String d = String.format("%02d", today.getDayOfMonth());

        // <root>/<area>/<yyyy>/<MM>/<dd>/<uuid>.<ext>
        Path baseDir = Path.of(storageRoot, area.folder(), y, m, d);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new ConflictException("Não foi possível criar diretório de upload");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = baseDir.resolve(filename).normalize();

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            // URL pública: /files/<area>/<yyyy>/<MM>/<dd>/<uuid>.<ext>
            String url = String.format("%s/%s/%s/%s/%s/%s",
                    StringUtils.trimTrailingCharacter(publicBaseUrl, '/'),
                    area.folder(), y, m, d, filename);
            return url;
        } catch (IOException e) {
            throw new ConflictException("Falha ao salvar arquivo: " + e.getMessage());
        }
    }

    @Override
    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;

        String base = StringUtils.trimTrailingCharacter(publicBaseUrl, '/'); // ex.: /files
        if (!publicUrl.startsWith(base + "/")) {
            // fora do nosso escopo: segurança/ignorar
            return;
        }
        // remove o prefixo "/files/"
        String relative = publicUrl.substring((base + "/").length());
        // quebra em segmentos para construir o Path de forma portátil
        String[] segments = relative.split("/"); // ["expenses","yyyy","MM","dd","file.ext"]
        Path target = Path.of(storageRoot, segments);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // opcional: logar. Não propague para não quebrar o fluxo chamador.
        }
    }

}
