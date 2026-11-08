package com.institucion.reservas.e2e.serenity;

import java.time.LocalDateTime;

import com.institucion.reservas.e2e.serenity.questions.TheAlertText;
import com.institucion.reservas.e2e.serenity.questions.TheConfirmationMessage;
import com.institucion.reservas.e2e.serenity.questions.TheStatusText;
import com.institucion.reservas.e2e.serenity.tasks.CheckAvailability;
import com.institucion.reservas.e2e.serenity.tasks.CompleteReservation;
import com.institucion.reservas.e2e.serenity.tasks.FillReservationRange;
import com.institucion.reservas.e2e.serenity.tasks.OpenReservationPage;
import com.institucion.reservas.e2e.serenity.tasks.PrepareConflictData;
import com.institucion.reservas.e2e.serenity.tasks.PrepareReservationData;
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
 * Un usuario activo reserva un recurso disponible desde la interfaz y ve
 * la confirmación (HU-28, espejo del flujo feliz de `reservation.spec.ts`).
 */
@Tag("serenity")
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Cuando se reserva un recurso desde la interfaz")
class WhenReservingAResourceTest {

    private Actor user;

    @BeforeEach
    void setUp() {
        user = Actor.named("Usuario");
        user.can(BrowseTheWebWithPlaywright.usingTheDefaultConfiguration());
    }

    @Test
    @DisplayName("debería mostrar la reserva confirmada")
    void shouldShowConfirmedReservation() {
        user.attemptsTo(PrepareReservationData.andResource());
        String resourceName = user.recall("recurso");
        user.attemptsTo(
                OpenReservationPage.forPreparedResource(),
                FillReservationRange.tomorrowMorning(),
                CompleteReservation.submit(),
                Ensure.that(TheConfirmationMessage.text()).contains("Reserva confirmada"),
                Ensure.that(TheConfirmationMessage.text()).contains(resourceName));
    }

    @Test
    @DisplayName("debería mostrar el conflicto si el intervalo ya está ocupado")
    void shouldShowConflictWhenIntervalIsTaken() {
        user.attemptsTo(
                PrepareConflictData.withExistingReservation(),
                OpenReservationPage.forPreparedResource(),
                FillReservationRange.tomorrowMorning(),
                CompleteReservation.submit(),
                Ensure.that(TheAlertText.value()).contains("El recurso ya está reservado en ese horario"));
    }

    @Test
    @DisplayName("debería rechazar un rango con fin anterior al inicio")
    void shouldRejectRangeEndingBeforeItStarts() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
                .withSecond(0).withNano(0);
        user.attemptsTo(
                PrepareReservationData.andResource(),
                OpenReservationPage.forPreparedResource(),
                FillReservationRange.fromTo(start, start.minusHours(1)),
                CompleteReservation.submit(),
                Ensure.that(TheAlertText.value())
                        .contains("La fecha de fin debe ser posterior a la fecha de inicio."));
    }

    @Test
    @DisplayName("debería mostrar la disponibilidad antes de reservar")
    void shouldShowAvailabilityBeforeReserving() {
        user.attemptsTo(
                PrepareReservationData.andResource(),
                OpenReservationPage.forPreparedResource(),
                CheckAvailability.forTomorrowMorning(),
                Ensure.that(TheStatusText.containing("disponible"))
                        .contains("El recurso está disponible en ese intervalo."));
    }
}
