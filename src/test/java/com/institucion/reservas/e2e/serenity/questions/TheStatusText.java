package com.institucion.reservas.e2e.serenity.questions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Texto del aviso de estado (`role="status"`) que contenga el fragmento
 * dado, para distinguirlo de otros avisos de la página.
 */
public final class TheStatusText {

    public static Question<String> containing(String fragment) {
        return Question.about("el aviso de estado con '" + fragment + "'").answeredBy(actor -> {
            Locator status = BrowseTheWebWithPlaywright.as(actor).getCurrentPage()
                    .locator("[role='status']")
                    .filter(new Locator.FilterOptions().setHasText(fragment))
                    .first();
            status.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            return status.textContent();
        });
    }

    private TheStatusText() {
    }
}
