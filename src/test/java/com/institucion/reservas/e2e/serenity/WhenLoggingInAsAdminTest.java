package com.institucion.reservas.e2e.serenity;

import com.institucion.reservas.e2e.serenity.questions.TheCurrentUrl;
import com.institucion.reservas.e2e.serenity.tasks.LoginAsAdmin;
import com.institucion.reservas.e2e.serenity.tasks.Logout;
import com.institucion.reservas.e2e.serenity.tasks.WaitForUrl;
import com.institucion.reservas.e2e.serenity.ui.AdminUsersPageTargets;
import net.serenitybdd.screenplay.playwright.interactions.Open;
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
 * El administrador inicia sesión por la interfaz y llega al panel de
 * administración de usuarios (HU-28, espejo de `login.spec.ts`).
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando el administrador inicia sesión por la interfaz")
class WhenLoggingInAsAdminTest {

    private Actor admin;

    @BeforeEach
    void setUp() {
        admin = Actor.named("Administrador");
        admin.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería llegar al panel de administración de usuarios")
    void shouldReachAdminUsersPanel() {
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(AdminUsersPageTargets.HEADING)
                        .hasText("Administración de usuarios"),
                Ensure.that(TheCurrentUrl.value()).contains("/admin/usuarios"));
    }

    @Test
    @DisplayName("debería volver al login tras cerrar la sesión")
    void shouldReturnToLoginAfterLogout() {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        admin.attemptsTo(
                LoginAsAdmin.withDefaultCredentials(),
                Logout.closeSession(),
                Ensure.that(TheCurrentUrl.value()).contains("/login"),
                Open.url(baseUrl + "/admin/usuarios"),
                WaitForUrl.containing("/login"),
                Ensure.that(TheCurrentUrl.value()).contains("/login"));
    }
}
