package com.institucion.reservas.e2e.serenity.tasks;

import com.microsoft.playwright.Page;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Espera a que la URL actual contenga el fragmento indicado. La navegación
 * de la SPA ocurre en segundo plano: sin esta espera, la aserción sobre la
 * URL corre antes de que el router la actualice y el test queda intermitente.
 */
public class WaitForUrl implements Task {

    private static final double TIMEOUT_MILLIS = 15_000;

    private final String fragment;

    private WaitForUrl(String fragment) {
        this.fragment = fragment;
    }

    @Override
    @Step("{0} espera a que la URL contenga #fragment")
    public <T extends Actor> void performAs(T actor) {
        Page page = BrowseTheWebWithPlaywright.as(actor).getCurrentPage();
        page.waitForFunction(
                "fragment => window.location.href.includes(fragment)",
                fragment,
                new Page.WaitForFunctionOptions().setTimeout(TIMEOUT_MILLIS));
    }

    public static WaitForUrl containing(String fragment) {
        return new WaitForUrl(fragment);
    }
}
