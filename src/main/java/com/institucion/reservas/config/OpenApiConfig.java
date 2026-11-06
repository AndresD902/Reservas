package com.institucion.reservas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos del contrato OpenAPI de la API de reservas.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sistema de reservas",
                version = "0.0.1-SNAPSHOT",
                description = """
                        API REST para gestionar la reserva de espacios físicos y recursos.
                        Los errores se devuelven con la estructura común status, error y message.
                        """
        )
)
public class OpenApiConfig {
}
