package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.SelectFromOptions;

/**
 * Cambia el estado del recurso desde su detalle y actualiza.
 */
public class ChangeResourceState implements Task {

    private final String state;

    private ChangeResourceState(String state) {
        this.state = state;
    }

    @Override
    @Step("{0} cambia el estado del recurso a #state")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                SelectFromOptions.byValue(state).from(AdminFormTargets.STATUS_SELECT),
                Click.on(AdminFormTargets.UPDATE_STATUS_BUTTON));
    }

    public static ChangeResourceState toMaintenance() {
        return new ChangeResourceState("MANTENIMIENTO");
    }
}
