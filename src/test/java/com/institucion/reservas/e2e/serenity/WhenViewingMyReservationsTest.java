package com.institucion.reservas.e2e.serenity;

import com.institucion.reservas.e2e.serenity.questions.TheStatusText;
import com.institucion.reservas.e2e.serenity.tasks.CancelReservation;
import com.institucion.reservas.e2e.serenity.tasks.OpenMyReservations;
import com.institucion.reservas.e2e.serenity.tasks.PrepareCancellableReservation;
import com.institucion.reservas.e2e.serenity.ui.MyReservationsTargets;
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
 * Un usuario consulta y cancela una reserva activa desde la interfaz
 * (HU-29, espejo de `my-reservations.spec.ts`).
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando se consultan mis reservas")
class WhenViewingMyReservationsTest {

    private Actor user;

    @BeforeEach
    void setUp() {
        user = Actor.named("Usuario");
        user.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería cancelar la reserva activa desde la interfaz")
    void shouldCancelActiveReservationFromInterface() {
        user.attemptsTo(PrepareCancellableReservation.active());
        String resourceName = user.recall("recurso");
        user.attemptsTo(
                OpenMyReservations.forPreparedUser(),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(
                        MyReservationsTargets.reservationArticle(resourceName, "Activa")).isVisible(),
                CancelReservation.active(),
                Ensure.that(TheStatusText.containing("cancelada"))
                        .contains("Reserva cancelada correctamente."),
                net.serenitybdd.screenplay.playwright.assertions.Ensure.that(
                        MyReservationsTargets.reservationArticle(resourceName, "Cancelada")).isVisible());
    }
}
