package com.institucion.reservas.e2e.serenity;

import java.util.UUID;

import com.institucion.reservas.e2e.serenity.questions.TheAlertText;
import com.institucion.reservas.e2e.serenity.questions.TheStatusText;
import com.institucion.reservas.e2e.serenity.tasks.ChangeResourceState;
import com.institucion.reservas.e2e.serenity.tasks.EditResourceName;
import com.institucion.reservas.e2e.serenity.tasks.LoginAsAdmin;
import com.institucion.reservas.e2e.serenity.tasks.OpenAdminResources;
import com.institucion.reservas.e2e.serenity.tasks.PrepareResourceWithFutureReservation;
import com.institucion.reservas.e2e.serenity.tasks.RegisterResource;
import com.institucion.reservas.e2e.serenity.tasks.ViewEntityDetail;
import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Administración de recursos desde la interfaz: registro, edición y
 * mantenimiento, incluido el bloqueo con reservas futuras (HU-29, espejo
 * de `admin.spec.ts`).
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando se administran recursos desde la interfaz")
class WhenAdministeringResourcesTest {

    private Actor admin;

    @BeforeEach
    void setUp() {
        admin = Actor.named("Administrador");
        admin.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería registrar un recurso, editarlo y ponerlo en mantenimiento")
    void shouldRegisterEditAndMaintainResource() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "000 Recurso Admin UI " + suffix;
        String updatedName = name + " actualizado";
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                RegisterResource.with(name),
                Ensure.that(TheStatusText.containing("registrado"))
                        .contains("Recurso registrado correctamente."),
                ViewEntityDetail.of(name),
                EditResourceName.to(updatedName),
                Ensure.that(TheStatusText.containing("actualizado"))
                        .contains("Recurso actualizado correctamente."),
                ChangeResourceState.toMaintenance(),
                Ensure.that(TheStatusText.containing("Estado del recurso"))
                        .contains("Estado del recurso actualizado."),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(
                        AdminFormTargets.entityRow(updatedName)).containsText("Mantenimiento"));
    }

    @Test
    @DisplayName("debería bloquear el mantenimiento si el recurso tiene reservas futuras")
    void shouldBlockMaintenanceWithFutureReservations() {
        admin.attemptsTo(PrepareResourceWithFutureReservation.futureReservation());
        String resourceName = admin.recall("recurso");
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                OpenAdminResources.page(),
                ViewEntityDetail.of(resourceName),
                ChangeResourceState.toMaintenance(),
                Ensure.that(TheAlertText.value()).contains(
                        "No se puede poner el recurso en mantenimiento porque tiene reservas activas futuras."));
    }
}
