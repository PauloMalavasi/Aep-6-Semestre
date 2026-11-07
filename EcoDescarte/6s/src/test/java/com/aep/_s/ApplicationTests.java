package com.aep._s;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"ecodescarte.dados-iniciais.enabled=false"})
class ApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private MongoDatabaseFactory databaseFactory;

	@Test
	void deveUsarBancoConfigurado() {
        String uri = System.getenv("MONGODB_URI");
        String esperado = uri == null ? "ecodescarte" : new com.mongodb.ConnectionString(uri).getDatabase();
        assertEquals(esperado, databaseFactory.getMongoDatabase().getName());
	}

    @Test
    void deveServirInterfaceEDocumentacaoOpenApi() throws Exception {
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            var pagina = get(client, "/");
            assertEquals(200, pagina.statusCode());
            assertTrue(pagina.body().contains("EcoDescarte"));
            var swagger = get(client, "/docs");
            assertEquals(200, swagger.statusCode());
            assertTrue(swagger.body().contains("Swagger UI"));
            var api = get(client, "/v3/api-docs");
            assertEquals(200, api.statusCode());
            assertTrue(api.body().contains("EcoDescarte API"));
            assertTrue(api.body().contains("/ponto_coleta/{id}"));
        }
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

}
