package com.institucion.reservas.e2e.serenity.questions;

import com.microsoft.playwright.Locator;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Texto del aviso de estado (`role="status"`) que contenga el fragmento
 * dado, para distinguirlo de otros avisos de la página.
 */
public final class TheStatusText {

    public static Question<String> containing(String fragment) {
        return Question.about("el aviso de estado con '" + fragment + "'").answeredBy(actor ->
                BrowseTheWebWithPlaywright.as(actor).getCurrentPage()
                        .locator("[role='status']")
                        .filter(new Locator.FilterOptions().setHasText(fragment))
                        .first()
                        .textContent());
    }

    private TheStatusText() {
    }
}
