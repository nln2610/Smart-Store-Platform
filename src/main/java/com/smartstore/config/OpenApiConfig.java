package com.smartstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // ⭐ Cấu hình này cho phép nhập Bearer token
        //    vào ô "Authorize" trên Swagger UI
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Store Platform API")
                        .version("1.0")
                        .description("""
                        ## Smart Store Platform
                        Hệ thống quản lý cửa hàng thông minh với AI Integration.
        
                        ### Tính năng chính:
                        - 🔐 JWT Authentication & RBAC
                        - 📦 Quản lý sản phẩm & kho hàng
                        - 🛒 Bán hàng & hóa đơn
                        - 📊 Dashboard & báo cáo
                        - 🤖 Gợi ý sản phẩm (Market Basket Analysis)
                        - ✨ AI tóm tắt reviews (Gemini API)
                        - 💬 Chatbot tư vấn sản phẩm
                        """))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}