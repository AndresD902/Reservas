package com.institucion.reservas.e2e.serenity;

import java.util.UUID;

import com.institucion.reservas.e2e.serenity.questions.TheAlertText;
import com.institucion.reservas.e2e.serenity.questions.TheStatusText;
import com.institucion.reservas.e2e.serenity.tasks.LoginAsAdmin;
import com.institucion.reservas.e2e.serenity.tasks.PrepareExistingUser;
import com.institucion.reservas.e2e.serenity.tasks.RegisterUser;
import com.institucion.reservas.e2e.serenity.tasks.ViewEntityDetail;
import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.ensure.Ensure;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Administración de usuarios desde la interfaz: registro con cambio de
 * estado y error de documento duplicado (HU-29, espejo de `admin.spec.ts`).
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando se administran usuarios desde la interfaz")
class WhenAdministeringUsersTest {

    private Actor admin;

    @BeforeEach
    void setUp() {
        admin = Actor.named("Administrador");
        admin.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería registrar un usuario y cambiar su estado a inactivo")
    void shouldRegisterUserAndDeactivateIt() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "000 Usuario Admin UI " + suffix;
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                RegisterUser.with(name, "UI" + suffix, "ui." + suffix + "@example.com"),
                Ensure.that(TheStatusText.containing("registrado"))
                        .contains("Usuario registrado correctamente."),
                ViewEntityDetail.of(name),
                Click.on(AdminFormTargets.DEACTIVATE_USER_BUTTON),
                Ensure.that(TheStatusText.containing("Estado del usuario"))
                        .contains("Estado del usuario actualizado."),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(
                        AdminFormTargets.entityRow(name)).containsText("Inactivo"));
    }

    @Test
    @DisplayName("debería informar un documento duplicado en el formulario")
    void shouldReportDuplicateDocument() {
        admin.attemptsTo(PrepareExistingUser.forDuplicateDocument());
        String document = admin.recall("documento");
        String suffix = admin.recall("sufijo");
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                RegisterUser.with("Duplicado " + suffix, document,
                        "nuevo." + suffix + "@example.com"),
                Ensure.that(TheAlertText.value()).contains("Ya existe un usuario con ese documento."));
    }
}
