package com.institucion.reservas.quality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AzurePipelineContractTest {
    private static final Set<String> REQUIRED_STAGES = Set.of(
            "BackendQuality",
            "FrontendQuality",
            "BrowserE2E",
            "ZapSecurity"
    );

    @Test
    void pipelineRunsEveryQualityStageAndPublishesItsEvidence() throws IOException {
        List<JsonNode> stages = stages();

        Set<String> actualStages = stages.stream()
                .map(stage -> stage.path("stage").asText())
                .collect(Collectors.toSet());

        assertTrue(actualStages.containsAll(REQUIRED_STAGES),
                "El pipeline debe ejecutar backend, frontend, navegador y ZAP");
        assertTrue(stages.stream()
                        .filter(stage -> REQUIRED_STAGES.contains(stage.path("stage").asText()))
                        .allMatch(this::publishesPipelineArtifact),
                "Cada etapa debe publicar evidencia aunque una verificación falle");
    }

    @Test
    void everyJobRunsOnAMicrosoftHostedAgent() throws IOException {
        List<JsonNode> pools = stages().stream()
                .flatMap(stage -> StreamSupport.stream(stage.findValues("pool").spliterator(), false))
                .toList();

        assertTrue(!pools.isEmpty(), "Cada etapa debe declarar su pool");
        assertTrue(pools.stream().allMatch(pool -> !pool.hasNonNull("name") && !pool.has("demands")),
                "Ninguna etapa debe depender de un agente self-hosted: si el agente está "
                        + "desconectado el run queda encolado y el pipeline no termina");
        assertTrue(pools.stream().allMatch(pool -> !pool.path("vmImage").asText().isBlank()),
                "Cada etapa debe fijar un vmImage hospedado por Microsoft");
    }

    private List<JsonNode> stages() throws IOException {
        JsonNode pipeline = new YAMLMapper().readTree(Path.of("azure-pipelines.yml").toFile());
        return StreamSupport.stream(pipeline.path("stages").spliterator(), false).toList();
    }

    private boolean publishesPipelineArtifact(JsonNode stage) {
        return StreamSupport.stream(stage.findValues("task").spliterator(), false)
                .anyMatch(task -> task.asText().equals("PublishPipelineArtifact@1"));
    }
}
