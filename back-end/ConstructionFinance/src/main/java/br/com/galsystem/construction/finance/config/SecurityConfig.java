package br.com.galsystem.construction.finance.config;
import br.com.galsystem.construction.finance.security.jwt.JwtAuthFilter;
import br.com.galsystem.construction.finance.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        // use UMA lista; evite múltiplas chamadas que se sobrescrevem
        cfg.setAllowedOrigins(List.of(
                "http://localhost:9090",
                "https://localhost:9090",
                "http://apiconstrucao.galsystems.com.br",
                "http://apiconstrucao.galsystems.com.br:9090",
                "https://apiconstrucao.galsystems.com.br",
                "https://apiconstrucao.galsystems.com.br:9090",
                "https://construcao.galsystems.com.br",
                "http://construcao.galsystems.com.br",
                "http://localhost:4200"
        ));
        // Se precisar de curingas com credentials:
        // cfg.setAllowedOriginPatterns(List.of("https://*.galsystems.com.br","http://localhost:*","https://localhost:*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS","HEAD"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Location","Content-Disposition","Authorization"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Rotas públicas (sem autenticação)
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/reset-password",           // caso a view HTML esteja fora do /auth
                                "/error"
                        ).permitAll()

                        // 🔓 Swagger e documentação
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 🔓 Recursos públicos (arquivos)
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/files/**").permitAll()

                        // 🔐 Necessitam autenticação
                        .requestMatchers(HttpMethod.POST, "/uploads/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/expenses/*/attachment").authenticated()

                        // ❌ Remova ou ajuste esta linha antiga (ela bloqueava o fluxo)
                        // .requestMatchers(HttpMethod.PUT, "/reset-password**").authenticated()

                        // 🔓 Página inicial
                        .requestMatchers("/", "/index.html", "/index").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // 🔒 Qualquer outra rota: autenticada
                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("""
                    {"status":403,"message":"Acesso negado","data":null,"erros":[]}
                """);
                        })
                );

        return http.build();
    }}
