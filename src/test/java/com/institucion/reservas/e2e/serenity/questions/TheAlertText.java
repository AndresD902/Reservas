package com.institucion.reservas.e2e.serenity.questions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Texto del mensaje de error visible (`role="alert"`).
 */
public final class TheAlertText {

    public static Question<String> value() {
        return Question.about("el mensaje de error").answeredBy(actor -> {
            Locator alert = BrowseTheWebWithPlaywright.as(actor).getCurrentPage()
                    .locator("[role='alert']")
                    .first();
            alert.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            return alert.textContent();
        });
    }

    private TheAlertText() {
    }
}
