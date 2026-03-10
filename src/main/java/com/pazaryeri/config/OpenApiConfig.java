package com.pazaryeri.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Dijital Pazar Yeri API",
        version = "1.0.0",
        description = """
            Yerel Üretici Platformu - RESTful API Dokümantasyonu
            
            ## Kullanıcı Rolleri
            - **SUPER_ADMIN**: Tüm sistem yönetimi, komisyon ve ayarlar
            - **ADMIN**: Üretici onayı, kullanıcı yönetimi
            - **PRODUCER**: Mağaza ve ürün yönetimi
            - **CONSUMER**: Ürün arama, sipariş verme
            """,
        contact = @Contact(name = "Platform Ekibi", email = "dev@pazaryeri.com")
    ),
    servers = {
        @Server(url = "/api", description = "API Sunucusu")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
