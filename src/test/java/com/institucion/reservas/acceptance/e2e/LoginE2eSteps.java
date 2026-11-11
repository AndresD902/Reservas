package com.institucion.reservas.acceptance.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.assertj.core.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Steps de los flujos Gherkin E2E de login (HU-31 y HU-32). Abre un contexto de
 * Playwright por escenario contra el stack de Docker Compose y graba el video
 * en build/e2e-videos.
 */
public class LoginE2eSteps {

    private static final String BASE_URL = System.getProperty("e2e.base.url", "http://localhost:5173")
            .replaceAll("/+$", "");
    private static final String ADMIN_EMAIL = System.getProperty("e2e.admin.email", "admin@example.com");
    private static final String ADMIN_PASSWORD = System.getProperty("e2e.admin.password", "admin123");
    private static final boolean HEADLESS =
            Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
    private static final double SLOW_MO =
            Double.parseDouble(System.getProperty("playwright.slowmo", "0"));
    private static final Path VIDEO_DIR =
            Paths.get(System.getProperty("e2e.video.dir", "build/e2e-videos"));

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    public static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(HEADLESS)
                .setSlowMo(SLOW_MO));
    }

    @AfterAll
    public static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Before
    public void openContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setRecordVideoDir(VIDEO_DIR)
                .setRecordVideoSize(1280, 720));
        page = context.newPage();
    }

    @After
    public void closeContextAndVerifyVideo(Scenario scenario) throws Exception {
        Video video = page != null ? page.video() : null;
        if (context != null) {
            context.close();
        }

        if (video == null) {
            Assertions.fail("No se grabó el video del escenario " + scenario.getName());
        }

        Files.createDirectories(VIDEO_DIR);
        Path videoPath = VIDEO_DIR.resolve(videoFileName(scenario));
        Path originalVideo = video.path();
        video.saveAs(videoPath);
        if (!originalVideo.equals(videoPath)) {
            Files.deleteIfExists(originalVideo);
        }
        Assertions.assertThat(Files.exists(videoPath))
                .as("video del escenario %s", scenario.getName())
                .isTrue();

        page = null;
        context = null;
    }

    private static String videoFileName(Scenario scenario) {
        String name = scenario.getName().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return name + ".webm";
    }

    @Dado("que no tengo una sesión iniciada")
    public void queNoTengoSesionIniciada() {
        page.context().clearCookies();
    }

    @Dado("que abro la pantalla de inicio de sesión")
    public void abroPantallaInicioSesion() {
        page.navigate(BASE_URL + "/login");
        fieldCorreo().waitFor();
    }

    @Cuando("abro la ruta {string}")
    public void abroLaRuta(String ruta) {
        page.navigate(BASE_URL + ruta);
    }

    @Cuando("inicio sesión como administrador")
    public void inicioSesionComoAdministrador() {
        autenticarse(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Cuando("cierro la sesión")
    public void cierroLaSesion() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cerrar sesión"))
                .click();
        page.waitForURL("**/login");
    }

    @Entonces("soy redirigido a la ruta {string}")
    public void soyRedirigidoALaRuta(String ruta) {
        page.waitForURL("**" + ruta);
        Assertions.assertThat(page.url()).endsWith(ruta);
    }

    @Entonces("estoy en la ruta {string}")
    public void estoyEnLaRuta(String ruta) {
        Assertions.assertThat(page.url()).endsWith(ruta);
    }

    @Entonces("veo el encabezado {string}")
    public void veoElEncabezado(String texto) {
        Locator heading = page.locator("h1");
        heading.waitFor();
        Assertions.assertThat(heading.textContent()).contains(texto);
    }

    private void autenticarse(String correo, String password) {
        enviarCredenciales(correo, password);
        page.waitForURL("**/admin/usuarios");
    }

    private void enviarCredenciales(String correo, String password) {
        fieldCorreo().waitFor();
        fieldCorreo().fill(correo);
        fieldPassword().fill(password);
        page.locator("button[type='submit']").click();
    }

    private Locator fieldCorreo() {
        return page.locator("#login-correo");
    }

    private Locator fieldPassword() {
        return page.locator("#login-password");
    }
}
