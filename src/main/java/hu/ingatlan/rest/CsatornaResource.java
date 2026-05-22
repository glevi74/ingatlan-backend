package hu.ingatlan.rest;

import hu.ingatlan.dto.CsatornaDto;
import hu.ingatlan.service.CsatornaService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/csatornak")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Chat – Csatornák")
public class CsatornaResource {

    @Inject
    CsatornaService service;

    @GET
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Az aktuális felhasználó számára látható csatornák listája")
    public List<CsatornaDto.Response> list() {
        return service.listAll();
    }

    @POST
    @RolesAllowed({"IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Új PRIVAT vagy CSOPORT csatorna létrehozása")
    public Response create(@Valid CsatornaDto.CreateRequest dto) {
        return Response.status(Response.Status.CREATED).entity(service.create(dto)).build();
    }

    @GET
    @Path("/kapcsolt")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Entitáshoz kapcsolt csatorna lekérése vagy automatikus létrehozása")
    public CsatornaDto.Response getOrCreateKapcsolt(
            @QueryParam("ingatlanId")  UUID ingatlanId,
            @QueryParam("feladatId")   UUID feladatId,
            @QueryParam("ugyfelId")    UUID ugyfelId,
            @QueryParam("megbizasId")  UUID megbizasId) {
        return service.getOrCreateKapcsolt(ingatlanId, feladatId, ugyfelId, megbizasId);
    }

    @GET
    @Path("/{id}/uzenetek")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Csatorna üzenetei (opcionálisan: csak a 'since' időpont után küldöttek)")
    public List<CsatornaDto.UzenetResponse> getUzenetek(
            @PathParam("id") UUID id,
            @QueryParam("since") String since) {
        LocalDateTime sinceTime = since != null && !since.isBlank()
                ? LocalDateTime.parse(since)
                : null;
        return service.getUzenetek(id, sinceTime);
    }

    @POST
    @Path("/{id}/uzenetek")
    @RolesAllowed({"IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Üzenet küldése egy csatornába")
    public Response sendUzenet(@PathParam("id") UUID id,
                               @Valid CsatornaDto.UzenetRequest dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.sendUzenet(id, dto))
                .build();
    }
}
