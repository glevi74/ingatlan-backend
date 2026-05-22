package hu.ingatlan.rest;

import hu.ingatlan.dto.FelhasznaloDto;
import hu.ingatlan.service.FelhasznaloService;
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

@Path("/api/v1/felhasznalok")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Felhasználók")
public class FelhasznaloResource {

    @Inject
    FelhasznaloService service;

    @GET
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Felhasználók listája (ADMIN: mind, egyéb: csak saját iroda)")
    public List<FelhasznaloDto.Response> list() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Egy felhasználó lekérdezése")
    public FelhasznaloDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO"})
    @Operation(summary = "Felhasználó adatainak módosítása (nev, email, telefon, szerep)")
    public FelhasznaloDto.Response update(@PathParam("id") UUID id,
                                          @Valid FelhasznaloDto.UpdateRequest dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/aktiv")
    @RolesAllowed({"ADMIN", "IRODAVEZETO"})
    @Operation(summary = "Felhasználó aktiválása / tiltása")
    public FelhasznaloDto.Response setAktiv(@PathParam("id") UUID id,
                                             @QueryParam("aktiv") boolean aktiv) {
        return service.setAktiv(id, aktiv);
    }
}
