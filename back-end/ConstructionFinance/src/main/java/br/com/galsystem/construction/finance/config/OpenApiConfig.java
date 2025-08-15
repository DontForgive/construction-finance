package br.com.galsystem.construction.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Construction Finance API")
                        .version("v1")
                        .description("Documentação da API (Expense, Payer, Supplier, Categories, User, Upload)")
                        .license(new License().name("Apache 2.0")))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME,
                        new SecurityScheme().name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("br.com.galsystem.construction.finance.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi categoriesApi() {
        return GroupedOpenApi.builder()
                .group("categories")
                .packagesToScan("br.com.galsystem.construction.finance.controller.category")
                .build();
    }

    @Bean
    public GroupedOpenApi expensesApi() {
        return GroupedOpenApi.builder()
                .group("expenses")
                .packagesToScan("br.com.galsystem.construction.finance.controller.expense")
                .build();
    }

    @Bean
    public GroupedOpenApi payersApi() {
        return GroupedOpenApi.builder()
                .group("payers")
                .packagesToScan("br.com.galsystem.construction.finance.controller.payer")
                .build();
    }

    @Bean
    public GroupedOpenApi suppliersApi() {
        return GroupedOpenApi.builder()
                .group("suppliers")
                .packagesToScan("br.com.galsystem.construction.finance.controller.supplier")
                .build();
    }

    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .packagesToScan("br.com.galsystem.construction.finance.controller.user")
                .build();
    }
}
