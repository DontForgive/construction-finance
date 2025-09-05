package br.com.galsystem.construction.finance.service.file;

import br.com.galsystem.construction.finance.files.UploadArea;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Salva o arquivo sob a área informada e retorna a URL pública.
     * Padrão: /files/{area}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
     */
    String store(UploadArea area, MultipartFile file);

    String createByFile(UploadArea area, MultipartFile file);

    /**
     * Remove um arquivo a partir da URL pública (ignora se não existir).
     */
    void deleteByPublicUrl(String publicUrl);
}