package hu.ingatlan.rest;

import hu.ingatlan.domain.Ajanlat;
import hu.ingatlan.dto.AjanlatDto;
import hu.ingatlan.service.AjanlatService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/ajanlatok")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ajánlatok", description = "Ajánlatkezelés")
public class AjanlatResource {

    @Inject
    AjanlatService service;

    @GET
    @Operation(summary = "Ajánlatok listázása (szűrés: megbizasId, ugyfelId)")
    public List<AjanlatDto.Response> list(
            @QueryParam("megbizasId") UUID megbizasId,
            @QueryParam("ugyfelId") UUID ugyfelId) {

        if (megbizasId != null) return service.findByMegbizas(megbizasId);
        if (ugyfelId != null) return service.findByUgyfel(ugyfelId);
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Ajánlat lekérdezése ID alapján")
    public AjanlatDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Új ajánlat rögzítése")
    public Response create(@Valid AjanlatDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Ajánlat módosítása")
    public AjanlatDto.Response update(@PathParam("id") UUID id,
                                      @Valid AjanlatDto.Request dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Ajánlat státuszának módosítása (elfogadáshoz használd a /elfogad végpontot)")
    public AjanlatDto.Response changeStatus(@PathParam("id") UUID id,
                                            @QueryParam("status") Ajanlat.AjanlatStatus status) {
        if (status == null) throw new BadRequestException("A 'status' query paraméter kötelező.");
        return service.changeStatus(id, status);
    }

    @PATCH
    @Path("/{id}/elfogad")
    @Operation(summary = "Ajánlat elfogadása — elutasítja a többi ajánlatot, lezárja a megbízást")
    public AjanlatDto.Response elfogad(@PathParam("id") UUID id) {
        return service.elfogad(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Ajánlat törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
