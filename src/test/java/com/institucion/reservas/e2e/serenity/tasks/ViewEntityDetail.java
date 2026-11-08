package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;

/**
 * Abre el detalle de la entidad (usuario o recurso) desde su fila.
 */
public class ViewEntityDetail implements Task {

    private final String entityName;

    private ViewEntityDetail(String entityName) {
        this.entityName = entityName;
    }

    @Override
    @Step("{0} abre el detalle de #entityName")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Click.on(AdminFormTargets.viewDetailButton(entityName)));
    }

    public static ViewEntityDetail of(String entityName) {
        return new ViewEntityDetail(entityName);
    }
}
