package hu.ingatlan.rest;

import hu.ingatlan.domain.Szerzes;
import hu.ingatlan.dto.SzerzesDto;
import hu.ingatlan.service.SzerzesService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/szerzesek")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Szerzések", description = "Szerzéskezelés")
public class SzerzesResource {

    @Inject
    SzerzesService service;

    @GET
    @Operation(summary = "Szerzések listázása (szűrés: ajanlatId)")
    public List<SzerzesDto.Response> list(@QueryParam("ajanlatId") UUID ajanlatId) {
        if (ajanlatId != null) return List.of(service.findByAjanlat(ajanlatId));
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Szerzés lekérdezése ID alapján")
    public SzerzesDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Szerzés rögzítése elfogadott ajánlathoz")
    public Response create(@Valid SzerzesDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Szerzés módosítása")
    public SzerzesDto.Response update(@PathParam("id") UUID id,
                                      @Valid SzerzesDto.Request dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Szerzés státuszának módosítása")
    public SzerzesDto.Response changeStatus(@PathParam("id") UUID id,
                                            @QueryParam("status") Szerzes.SzerzesStatus status) {
        if (status == null) throw new BadRequestException("A 'status' query paraméter kötelező.");
        return service.changeStatus(id, status);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Szerzés törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
