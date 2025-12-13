package com.example.e_commerce_techshop.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce TechShop API")
                        .version("1.0.0")
                        .description("API documentation for E-Commerce TechShop application")
                        .contact(new Contact()
                                .name("TechShop Team")
                                .email("support@techshop.com")
                                .url("https://techshop.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development server"),
                        new Server()
                                .url("https://technova-d3gferhtgdaaaedh.eastasia-01.azurewebsites.net")
                                .description("Production server"),
                        new Server()
                                .url("{protocol}://{host}:{port}")
                                .description("Configurable server")
                                .variables(new io.swagger.v3.oas.models.servers.ServerVariables()
                                        .addServerVariable("protocol", 
                                            new io.swagger.v3.oas.models.servers.ServerVariable()
                                                ._default("https")
                                                ._enum(List.of("http", "https")))
                                        .addServerVariable("host", 
                                            new io.swagger.v3.oas.models.servers.ServerVariable()
                                                ._default("localhost"))
                                        .addServerVariable("port", 
                                            new io.swagger.v3.oas.models.servers.ServerVariable()
                                                ._default("8080")))))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token (without 'Bearer ' prefix)")));
    }
}