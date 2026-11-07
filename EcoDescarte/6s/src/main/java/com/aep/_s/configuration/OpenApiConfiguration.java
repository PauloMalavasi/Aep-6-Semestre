package com.aep._s.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI pontoApiConfiguration() {
        return new OpenAPI().info(new Info()
                .title("EcoDescarte API")
                .version("1.0")
                .description("PoC acadêmica para gerenciar pontos de coleta. Os três registros iniciais são dados fictícios de demonstração. API sem autenticação, destinada à execução local."));
    }
}
