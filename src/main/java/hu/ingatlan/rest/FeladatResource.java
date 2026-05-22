package hu.ingatlan.rest;

import hu.ingatlan.domain.Feladat;
import hu.ingatlan.dto.FeladatDto;
import hu.ingatlan.service.FeladatService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/feladatok")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Feladatok")
public class FeladatResource {

    @Inject
    FeladatService service;

    @GET
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Feladatok listája (opcionális szűrés: status, hozzarendeltId)")
    public List<FeladatDto.Response> list(
            @QueryParam("status") Feladat.FeladatStatus status,
            @QueryParam("hozzarendeltId") UUID hozzarendeltId) {
        return service.listAll(status, hozzarendeltId);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Egy feladat lekérdezése")
    public FeladatDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Új feladat létrehozása")
    public Response create(@Valid FeladatDto.Request dto) {
        return Response.status(Response.Status.CREATED).entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Feladat adatainak módosítása")
    public FeladatDto.Response update(@PathParam("id") UUID id, @Valid FeladatDto.Request dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Feladat státuszának módosítása")
    public FeladatDto.Response changeStatus(
            @PathParam("id") UUID id,
            @QueryParam("status") @DefaultValue("NYITOTT") Feladat.FeladatStatus status) {
        return service.changeStatus(id, status);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO"})
    @Operation(summary = "Feladat törlése (csak vezető+)")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
