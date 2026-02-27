package site.asm0dey.relay.server

import io.vertx.core.http.HttpServerRequest
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import org.jboss.resteasy.reactive.RestResponse
import site.asm0dey.relay.domain.Envelope
import java.util.*
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder as RestResponseBuilder

@Path("")
class HttpEndpoint {
    @Inject
    lateinit var socketService: SocketService

    @Context
    lateinit var request: HttpServerRequest

    @GET
    suspend fun get(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
    ): RestResponse<ByteArray?> {
        val envelope = envelope("GET")
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @POST
    suspend fun post(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
        body: ByteArray?
    ): RestResponse<ByteArray?> {
        val envelope = envelope("POST", body)
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @PUT
    suspend fun put(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
        body: ByteArray?
    ): RestResponse<ByteArray?> {
        val envelope = envelope("PUT", body)
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @HEAD
    suspend fun head(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
    ): RestResponse<ByteArray?> {
        val envelope = envelope("HEAD")
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @DELETE
    suspend fun delete(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
        body: ByteArray?
    ): RestResponse<ByteArray?> {
        val envelope = envelope("DELETE", body)
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @OPTIONS
    suspend fun options(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
    ): RestResponse<ByteArray?> {
        val envelope = envelope("OPTIONS")
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    @PATCH
    suspend fun patch(
        @QueryParam("X-Domain") domain: String?,
        @HeaderParam("X-Domain") domainHeader: String?,
        body: ByteArray?
    ): RestResponse<ByteArray?> {
        val envelope = envelope("PATCH", body)
        val responseEnvelope = socketService.request(envelope, extractHost(domain, domainHeader))
        return buildResponse(responseEnvelope)
    }

    private fun envelope(method: String, body: ByteArray? = null): Envelope = Envelope(
        correlationId = UUID.randomUUID().toString(),
        payload = site.asm0dey.relay.domain.Request(
            site.asm0dey.relay.domain.Request.RequestPayload(
                method = method,
                path = request.path(),
                query = request
                    .query()
                    ?.split('&')
                    ?.filterNot { it.startsWith("X-Domain") }
                    ?.mapNotNull { param ->
                        val parts = param.split('=', limit = 2)
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }
                    ?.toMap() ?: hashMapOf(),
                headers = request.headers().entries().filterNot { it.key.startsWith("X-Domain") }
                    .associate { it.key to it.value },
                body = body
            )
        )
    )

    private fun buildResponse(envelope: Envelope): RestResponse<ByteArray?> {
        val responsePayload = envelope.payload as? site.asm0dey.relay.domain.Response
            ?: throw IllegalStateException("Expected Response payload, got ${envelope.payload}")
        val payload = responsePayload.value

        val status = RestResponse.Status.fromStatusCode(payload.statusCode)

        var builder = RestResponseBuilder.create<ByteArray?>(status).entity(payload.body)
        payload.headers.forEach { (key, value) ->
            builder = builder.header(key, value)
        }
        return builder.build()
    }


    private fun extractHost(domain: String?, domainHeader: String?): String =
        (domain ?: domainHeader ?: request.getHeader("Host"))
            ?.substringBefore('.') ?: throw IllegalStateException("No host found")
}