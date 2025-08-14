package br.com.galsystem.construction.finance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Value("${app.files.storage-root}")
    private String storageRoot;

    @Value("${app.files.public-base-url}")
    private String publicBaseUrl; // ex.: /files

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ex.: C:/ConstructionFinance/app-data/uploads  ->  file:C:/ConstructionFinance/app-data/uploads/
        String root = Paths.get(storageRoot).toAbsolutePath().normalize().toString();
        String location = "file:" + (root.endsWith("/") || root.endsWith("\\") ? root : root + "/");

        registry.addResourceHandler(publicBaseUrl.endsWith("/") ? publicBaseUrl + "**" : publicBaseUrl + "/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
