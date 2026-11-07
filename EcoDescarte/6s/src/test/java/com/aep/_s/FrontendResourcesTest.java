package com.aep._s;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendResourcesTest {

    @Test
    void deveDisponibilizarOsArquivosDaInterface() throws IOException {
        ClassPathResource pagina = new ClassPathResource("static/index.html");
        ClassPathResource estilos = new ClassPathResource("static/css/styles.css");
        ClassPathResource javascript = new ClassPathResource("static/js/app.js");

        assertTrue(pagina.exists());
        assertTrue(estilos.exists());
        assertTrue(javascript.exists());

        String html = pagina.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("EcoDescarte"));
        assertTrue(html.contains("css/styles.css"));
        assertTrue(html.contains("js/app.js"));
    }
}
