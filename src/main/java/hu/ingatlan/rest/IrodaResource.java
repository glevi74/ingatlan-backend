package hu.ingatlan.rest;

import hu.ingatlan.dto.IrodaDto;
import hu.ingatlan.service.IrodaService;
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

@Path("/api/v1/irodak")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Irodák", description = "Iroda (tenant) kezelés")
public class IrodaResource {

    @Inject
    IrodaService service;

    @GET
    @RolesAllowed("ADMIN")
    @Operation(summary = "Az összes iroda listázása (csak ADMIN)")
    public List<IrodaDto.Response> list() {
        return service.listAll();
    }

    @GET
    @Path("/sajat")
    @RolesAllowed({"IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Saját iroda adatai")
    public IrodaDto.Response sajat() {
        return service.findSajat();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Iroda lekérdezése ID alapján")
    public IrodaDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Új iroda létrehozása (csak ADMIN)")
    public Response create(@Valid IrodaDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO"})
    @Operation(summary = "Iroda adatainak módosítása (ADMIN: bármely, IRODAVEZETO: csak saját)")
    public IrodaDto.Response update(@PathParam("id") UUID id, @Valid IrodaDto.Request dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Iroda törlése (csak ADMIN)")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
