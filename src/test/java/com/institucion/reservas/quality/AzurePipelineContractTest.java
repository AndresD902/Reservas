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
            "ZapSecurity",
            "PerformanceEvidence"
    );

    @Test
    void pipelineRunsEveryQualityStageAndPublishesItsEvidence() throws IOException {
        JsonNode pipeline = new YAMLMapper().readTree(Path.of("azure-pipelines.yml").toFile());
        List<JsonNode> stages = StreamSupport.stream(pipeline.path("stages").spliterator(), false).toList();
        Set<String> actualStages = stages.stream()
                .map(stage -> stage.path("stage").asText())
                .collect(Collectors.toSet());

        assertTrue(actualStages.containsAll(REQUIRED_STAGES),
                "El pipeline debe ejecutar backend, frontend, navegador, ZAP y rendimiento");
        assertTrue(stages.stream()
                        .filter(stage -> REQUIRED_STAGES.contains(stage.path("stage").asText()))
                        .allMatch(this::publishesPipelineArtifact),
                "Cada etapa debe publicar evidencia aunque una verificación falle");
    }

    @Test
    void performanceStageRunsForMainPushesOrWhenManuallyRequested() throws IOException {
        JsonNode pipeline = new YAMLMapper().readTree(Path.of("azure-pipelines.yml").toFile());
        JsonNode parameter = StreamSupport.stream(pipeline.path("parameters").spliterator(), false)
                .filter(item -> item.path("name").asText().equals("runPerformance"))
                .findFirst()
                .orElse(null);
        JsonNode performanceStage = StreamSupport.stream(pipeline.path("stages").spliterator(), false)
                .filter(stage -> stage.path("stage").asText().equals("PerformanceEvidence"))
                .findFirst()
                .orElse(null);

        assertTrue(parameter != null && parameter.path("default").asBoolean(true) == false,
                "JMeter debe quedar manual por defecto en las validaciones de pull request");
        assertTrue(performanceStage != null
                        && performanceStage.path("condition").asText().contains("runPerformance"),
                "JMeter debe ejecutarse al pedirlo manualmente o en el push a main");
    }

    private boolean publishesPipelineArtifact(JsonNode stage) {
        return StreamSupport.stream(stage.findValues("task").spliterator(), false)
                .anyMatch(task -> task.asText().equals("PublishPipelineArtifact@1"));
    }
}
