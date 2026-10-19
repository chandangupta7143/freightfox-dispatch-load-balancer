package com.chandan.freightfox.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration metadata.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FreightFox Dispatch Load Balancer API")
                        .version("1.0.0")
                        .description("REST API for FreightFox Logistics dispatch optimization. "
                                + "Accepts delivery orders and fleet vehicles, validates constraints, "
                                + "and computes load-balanced dispatch routes prioritizing urgent orders "
                                + "and minimizing total Haversine travel distance.")
                        .contact(new Contact()
                                .name("FreightFox Engineering")
                                .email("engineering@freightfox.com")));
    }
}
