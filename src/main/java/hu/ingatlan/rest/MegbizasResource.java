package hu.ingatlan.rest;

import hu.ingatlan.domain.Megbizas;
import hu.ingatlan.dto.MegbizasDto;
import hu.ingatlan.service.MegbizasService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/megbizasok")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Megbízások", description = "Megbízáskezelés")
public class MegbizasResource {

    @Inject
    MegbizasService service;

    @GET
    @Operation(summary = "Megbízások listázása (szűrés: ugyfelId, ingatlanId, aktivOnly)")
    public List<MegbizasDto.Response> list(
            @QueryParam("ugyfelId") UUID ugyfelId,
            @QueryParam("ingatlanId") UUID ingatlanId,
            @QueryParam("aktivOnly") @DefaultValue("false") boolean aktivOnly) {

        if (ugyfelId != null) return service.findByUgyfel(ugyfelId);
        if (ingatlanId != null) return service.findByIngatlan(ingatlanId);
        if (aktivOnly) return service.listAktivak();
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Megbízás lekérdezése ID alapján")
    public MegbizasDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Új megbízás létrehozása")
    public Response create(@Valid MegbizasDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Megbízás módosítása")
    public MegbizasDto.Response update(@PathParam("id") UUID id,
                                       @Valid MegbizasDto.Request dto) {
        return service.update(id, dto);
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Megbízás státuszának módosítása")
    public MegbizasDto.Response changeStatus(@PathParam("id") UUID id,
                                             @QueryParam("status") Megbizas.MegbizasStatus status) {
        if (status == null) throw new BadRequestException("A 'status' query paraméter kötelező.");
        return service.changeStatus(id, status);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Megbízás törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
