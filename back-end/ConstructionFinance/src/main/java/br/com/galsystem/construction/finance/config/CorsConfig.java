package br.com.galsystem.construction.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:9090",
                                "https://localhost:9090",
                                "http://apiconstrucao.galsystems.com.br",
                                "http://apiconstrucao.galsystems.com.br:9090",
                                "https://apiconstrucao.galsystems.com.br",
                                "https://apiconstrucao.galsystems.com.br:9090",
                                "https://construcao.galsystems.com.br",
                                "http://construcao.galsystems.com.br"
                        )
                        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

}
