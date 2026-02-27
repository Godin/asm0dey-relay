package site.asm0dey.relay.server

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import io.quarkiverse.wiremock.devservice.ConnectWireMock
import io.quarkiverse.wiremock.devservice.WireMockConfigKey
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.websockets.next.WebSocketConnector
import io.restassured.RestAssured.given
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import picocli.CommandLine
import site.asm0dey.relay.client.Client
import site.asm0dey.relay.client.WsClient
import java.net.URI


@QuarkusTest
@ConnectWireMock
class FirstTest {
    @TestHTTPResource
    lateinit var baseUri: URI

    lateinit var wireMock: WireMock

    @ConfigProperty(name = WireMockConfigKey.PORT)
    lateinit var wiremockPort: Integer

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    fun parseResult(): CommandLine.ParseResult {
        return CommandLine(Client()).parseArgs(
            "--secret",
            "Secret",
            "--insecure",
            "-l",
            "localhost",
            "-h",
            "localhost",
            "-r",
            System.getProperty("quarkus.http.test-port", "8081"),
            "--domain",
            "test",
            wiremockPort.toString()
        )
    }

    @Inject
    lateinit var connector: WebSocketConnector<WsClient>

    @Test
    fun testGet() {
        println("DEBUG: Testing connection to localhost:${baseUri.port}")
        try {
            java.net.Socket("127.0.0.1", baseUri.port).use {
                println("DEBUG: Socket connected to 127.0.0.1:${baseUri.port}: ${it.isConnected}")
            }
        } catch (e: Exception) {
            println("DEBUG: Socket failed to 127.0.0.1:${baseUri.port}: ${e.message}")
        }

        val wsUri = baseUri()
        println("DEBUG: wsUri=$wsUri")
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(get(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody("OK")))
        given()
            .header("X-Domain", "test")
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(`is`("OK"))
    }

    @Test
    fun testPost() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(post(urlEqualTo("/")).willReturn(aResponse().withStatus(201).withBody("Created")))
        given()
            .header("X-Domain", "test")
            .body("test body")
            .`when`()
            .post("/")
            .then()
            .statusCode(201)
            .body(`is`("Created"))
    }

    @Test
    fun testPut() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(put(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody("Updated")))
        given()
            .header("X-Domain", "test")
            .body("update body")
            .`when`()
            .put("/")
            .then()
            .statusCode(200)
            .body(`is`("Updated"))
    }

    @Test
    fun testDelete() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(delete(urlEqualTo("/")).willReturn(aResponse().withStatus(204)))
        given()
            .header("X-Domain", "test")
            .`when`()
            .delete("/")
            .then()
            .statusCode(204)
    }

    @Test
    fun testHead() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(head(urlEqualTo("/")).willReturn(aResponse().withStatus(200)))
        given()
            .header("X-Domain", "test")
            .`when`()
            .head("/")
            .then()
            .statusCode(200)
    }

    @Test
    fun testOptions() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(options(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody("Options OK")))
        given()
            .header("X-Domain", "test")
            .`when`()
            .options("/")
            .then()
            .statusCode(200)
            .body(`is`("Options OK"))
    }

    @Test
    fun testPatch() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(patch(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody("Patched")))
        given()
            .header("X-Domain", "test")
            .body("patch data")
            .`when`()
            .patch("/")
            .then()
            .statusCode(200)
            .body(`is`("Patched"))
    }

    @Test
    fun testLargePayload() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        val largePayload = "A".repeat(10 * 1024 * 1024)
        wireMock.register(
            post("/large")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody("A")
                )
        )
        given()
            .header("X-Domain", "test")
            .body(largePayload)
            .`when`()
            .post("http://localhost:$wiremockPort/large")
            .then()
            .statusCode(200)
            .body(`is`("A"))
    }

    @Test
    fun testHeaderPropagation() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(get(urlEqualTo("/")).willReturn(aResponse().withStatus(200).withBody("Headers OK")))
        given()
            .header("X-Domain", "test")
            .header("X-Custom-Header", "custom-value")
            .header("Authorization", "Bearer token123")
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(`is`("Headers OK"))
    }

    @Test
    fun testQueryParameterForwarding() {
        val wsUri = baseUri()
        connector.baseUri(URI(wsUri))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(get(urlPathMatching("/.*")).willReturn(aResponse().withStatus(200).withBody("Query OK")))
        given()
            .queryParam("X-Domain", "test")
            .queryParam("param1", "value1")
            .queryParam("param2", "value2")
            .`when`()
            .get("/")
            .then()
            .statusCode(200)
            .body(`is`("Query OK"))
    }

    @Test
    fun testErrorResponse() {
        connector.baseUri(URI(baseUri()))
            .pathParam("secret", "Secret")
            .customizeOptions { connectOptions, _ ->
                connectOptions.addHeader("domain", "test")
            }
            .connectAndAwait()
        wireMock.register(
            get(urlEqualTo("/")).willReturn(
                aResponse().withStatus(500).withBody("Internal Server Error")
            )
        )
        given()
            .header("X-Domain", "test")
            .`when`()
            .get("/")
            .then()
            .statusCode(500)
            .body(`is`("Internal Server Error"))
    }

    private fun baseUri(): String {
        val wsUri = baseUri.toString().replace("http://", "ws://").replace("localhost", "127.0.0.1")
        return wsUri
    }
}


class RequestSizeTransformer() : ResponseTransformerV2 {

    override fun getName(): String = "request-size-transformer"

    override fun transform(
        response: Response,
        serveEvent: ServeEvent
    ): Response? {
        val bodySize = serveEvent.request.body.size
        return Response.Builder.like(response)
            .but()
            .body("Request body size: $bodySize bytes")
            .build()
    }

    override fun applyGlobally(): Boolean = false
}
