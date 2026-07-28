package com.josemaba.marquesitasapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marquesitasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marquesitas API")
                        .description("REST API for administering a snack food truck: categories, products, "
                                + "addons, product-addon associations and orders with automatic subtotal/total calculation.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Jose Barraza").email("Josemaba_22@hotmail.com"))
                        .license(new License().name("Proprietary")));
    }
}
