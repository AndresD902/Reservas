package com.institucion.reservas.quality;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlledFailureEvidenceTest {
    @Test
    @EnabledIfSystemProperty(named = "hu36.forceFailure", matches = "true")
    void reportsAnIdentifiableAssertionFailureWhenExplicitlyEnabled() {
        assertEquals("resultado esperado", "resultado real",
                "Fallo controlado solicitado para evidenciar una aserción roja en Azure Pipelines");
    }
}
