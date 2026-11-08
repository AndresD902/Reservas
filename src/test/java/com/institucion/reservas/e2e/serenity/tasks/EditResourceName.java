package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.Enter;

/**
 * Edita el nombre del recurso desde su detalle y guarda los cambios.
 */
public class EditResourceName implements Task {

    private final String newName;

    private EditResourceName(String newName) {
        this.newName = newName;
    }

    @Override
    @Step("{0} edita el nombre del recurso a #newName")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(AdminFormTargets.EDIT_RESOURCE_BUTTON),
                Enter.theValue(newName).into(AdminFormTargets.EDIT_RESOURCE_NAME),
                Click.on(AdminFormTargets.SAVE_RESOURCE_BUTTON));
    }

    public static EditResourceName to(String newName) {
        return new EditResourceName(newName);
    }
}
