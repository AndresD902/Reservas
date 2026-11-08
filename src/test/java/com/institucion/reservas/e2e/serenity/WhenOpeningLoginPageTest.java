package com.institucion.reservas.e2e.serenity;

import com.institucion.reservas.e2e.serenity.questions.TheCurrentUrl;
import com.institucion.reservas.e2e.serenity.tasks.OpenLoginPage;
import com.institucion.reservas.e2e.serenity.ui.LoginPageTargets;
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
 * Historia mínima de validación (HU-27): Serenity + Playwright abren un
 * navegador real, navegan a la página de login y generan el reporte con
 * capturas. La tarea `serenityE2ETest` la selecciona por el tag `serenity`.
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando se abre la página de login")
class WhenOpeningLoginPageTest {

    private Actor guest;

    @BeforeEach
    void setUp() {
        guest = Actor.named("Invitado");
        guest.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería mostrar el formulario de inicio de sesión")
    void shouldShowLoginForm() {
        guest.attemptsTo(
                OpenLoginPage.onTheLoginPage(),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(LoginPageTargets.HEADING)
                        .hasText("Iniciar sesión"),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(LoginPageTargets.EMAIL_INPUT)
                        .isVisible(),
                Ensure.that(TheCurrentUrl.value()).contains("/login"));
    }
}
