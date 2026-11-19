
package com.serenity.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerenityE2ESmoke {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,1000");

        boolean headless = Boolean.parseBoolean(
                System.getenv().getOrDefault("SERENITY_E2E_HEADLESS", "false")
        );

        if (headless) {
            options.addArguments("--headless=new", "--disable-gpu");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void pantallaLoginMuestraLosCamposYElBotonDeAcceso() {
        driver.get(baseUrl() + "/login");

        wait.until(d -> d.findElement(By.id("correo")).isDisplayed());

        assertEquals(
                "Correo electrónico",
                driver.findElement(By.cssSelector("label[for='correo']"))
                        .getText().trim()
        );

        assertEquals(
                "Contraseña",
                driver.findElement(By.cssSelector("label[for='contrasena']"))
                        .getText().trim()
        );

        assertTrue(
                driver.findElement(By.cssSelector("button.boton-ingreso"))
                        .isDisplayed()
        );
    }

    @Test
    void enviarFormularioVacioMuestraLosErroresDeValidacion() {
        driver.get(baseUrl() + "/login");

        wait.until(d -> d.findElement(By.id("correo")).isDisplayed());

        driver.findElement(By.cssSelector("button.boton-ingreso")).click();

        wait.until(d ->
                d.findElements(By.cssSelector(".field .error")).size() >= 2
        );

        List<String> mensajes = driver
                .findElements(By.cssSelector(".field .error"))
                .stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();

        assertTrue(
                mensajes.contains("Ingresa un correo válido."),
                "Debe avisar que el correo no es válido."
        );

        assertTrue(
                mensajes.contains("Ingresa tu contraseña."),
                "Debe pedir la contraseña."
        );
    }

    @Test
    void usuarioDePruebaPuedeIniciarSesionYEntrarAlDashboard() {
        String correo = System.getenv("SERENITY_E2E_EMAIL");
        String contrasena = System.getenv("SERENITY_E2E_PASSWORD");

        Assumptions.assumeTrue(
                correo != null && !correo.isBlank()
                        && contrasena != null && !contrasena.isBlank(),
                "Define SERENITY_E2E_EMAIL y SERENITY_E2E_PASSWORD "
                        + "con una cuenta de prueba."
        );

        driver.get(baseUrl() + "/login");

        wait.until(d -> d.findElement(By.id("correo")).isDisplayed());

        driver.findElement(By.id("correo")).sendKeys(correo);
        driver.findElement(By.id("contrasena")).sendKeys(contrasena);
        driver.findElement(By.cssSelector("button.boton-ingreso")).click();

        try {
            wait.until(d -> d.getCurrentUrl().contains("/inicio"));

            wait.until(d ->
                    d.getPageSource().contains("¿Cómo te sientes hoy?")
            );

            assertTrue(
                    driver.getCurrentUrl().contains("/inicio"),
                    "Tras iniciar sesión, el usuario debe llegar al dashboard."
            );

            assertTrue(
                    driver.getPageSource().contains("¿Cómo te sientes hoy?"),
                    "El dashboard debe mostrar la sección de estado de ánimo."
            );

        } catch (org.openqa.selenium.TimeoutException e) {
            System.err.println("ERROR EN LA PRUEBA E2E DE INICIO DE SESIÓN");
            System.err.println("URL final: " + driver.getCurrentUrl());
            System.err.println("Título: " + driver.getTitle());
            System.err.println("Contenido HTML recibido:");

            // Evita imprimir datos sensibles del formulario.
            String html = driver.getPageSource()
                    .replaceAll(
                            "(?i)(<input[^>]*type=[\"']password[\"'][^>]*value=[\"'])[^\"']*([\"'])",
                            "$1[OCULTO]$2"
                    );

            System.err.println(html);
            throw e;
        }
    }

    private String baseUrl() {
        String configured = System.getProperty(
                "serenity.baseUrl",
                System.getenv().getOrDefault(
                        "SERENITY_BASE_URL",
                        "http://localhost:8080"
                )
        );

        return configured.replaceAll("/+$", "");
    }
}
