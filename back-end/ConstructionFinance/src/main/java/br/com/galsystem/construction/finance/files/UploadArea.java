package br.com.galsystem.construction.finance.files;
import java.util.Arrays;

public enum UploadArea {
    EXPENSES("expenses"),
    SUPPLIERS("suppliers"),
    CATEGORIES("categories");
    // adicione outras models aqui, ex.: USERS, INVOICES…

    private final String folder;

    UploadArea(String folder) { this.folder = folder; }

    public String folder() { return folder; }

    public static UploadArea from(String raw) {
        return Arrays.stream(values())
                .filter(a -> a.folder.equalsIgnoreCase(raw) || a.name().equalsIgnoreCase(raw))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Área de upload inválida: " + raw));
    }
}