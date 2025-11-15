package com.shopping.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("REST APIs for Payment Management in Online Shopping Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Payment Service Team")
                                .email("payment@shopping.com")));
    }
}