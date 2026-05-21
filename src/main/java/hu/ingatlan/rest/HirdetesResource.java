package hu.ingatlan.rest;

import hu.ingatlan.domain.Hirdetes;
import hu.ingatlan.dto.HirdetesDto;
import hu.ingatlan.service.HirdetesService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/hirdetesek")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hirdetések", description = "Hirdetéskezelés")
public class HirdetesResource {

    @Inject
    HirdetesService service;

    @GET
    @Operation(summary = "Hirdetések listázása (szűrés: megbizasId, aktivOnly)")
    public List<HirdetesDto.Response> list(
            @QueryParam("megbizasId") UUID megbizasId,
            @QueryParam("aktivOnly") @DefaultValue("false") boolean aktivOnly) {

        if (megbizasId != null) return service.findByMegbizas(megbizasId);
        if (aktivOnly) return service.listAktivak();
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Hirdetés lekérdezése ID alapján")
    public HirdetesDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Új hirdetés létrehozása")
    public Response create(@Valid HirdetesDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Hirdetés módosítása")
    public HirdetesDto.Response update(@PathParam("id") UUID id,
                                       @Valid HirdetesDto.Request dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Hirdetés státuszának módosítása")
    public HirdetesDto.Response changeStatus(@PathParam("id") UUID id,
                                             @QueryParam("status") Hirdetes.HirdetesStatus status) {
        if (status == null) throw new BadRequestException("A 'status' query paraméter kötelező.");
        return service.changeStatus(id, status);
    }

    @POST
    @Path("/{id}/megtekintes")
    @Operation(summary = "Megtekintésszámláló növelése")
    public HirdetesDto.Response megtekintes(@PathParam("id") UUID id) {
        return service.megtekintesNoveles(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Hirdetés törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
