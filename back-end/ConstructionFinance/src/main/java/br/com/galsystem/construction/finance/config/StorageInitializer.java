package br.com.galsystem.construction.finance.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class StorageInitializer {

    @Value("${app.files.storage-root}")
    private String storageRoot;

    @PostConstruct
    public void init() {
        File dir = new File(storageRoot);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("📂 Diretório criado: " + storageRoot);
            } else {
                System.err.println("⚠️ Falha ao criar diretório: " + storageRoot);
            }
        }
    }
}