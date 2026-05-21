package hu.ingatlan.rest;

import hu.ingatlan.domain.Ingatlan;
import hu.ingatlan.dto.IngatlanDto;
import hu.ingatlan.service.IngatlanService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/ingatlanok")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ingatlanok", description = "Ingatlankezelés")
public class IngatlanResource {

    @Inject
    IngatlanService service;

    @GET
    @Operation(summary = "Ingatlanok listázása / keresés")
    public List<IngatlanDto.Response> list(
            @QueryParam("tipus") Ingatlan.IngatlanTipus tipus,
            @QueryParam("minAlapterulet") Double minAlapterulet,
            @QueryParam("maxAlapterulet") Double maxAlapterulet,
            @QueryParam("minSzobaszam") Integer minSzobaszam) {

        if (tipus != null || minAlapterulet != null
                || maxAlapterulet != null || minSzobaszam != null) {
            IngatlanDto.SearchParams params = new IngatlanDto.SearchParams();
            params.setTipus(tipus);
            params.setMinAlapterulet(minAlapterulet);
            params.setMaxAlapterulet(maxAlapterulet);
            params.setMinSzobaszam(minSzobaszam);
            return service.search(params);
        }
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Ingatlan lekérdezése ID alapján")
    public IngatlanDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Új ingatlan létrehozása")
    public Response create(@Valid IngatlanDto.Request dto) {
        return Response.status(Response.Status.CREATED)
                .entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Ingatlan módosítása")
    public IngatlanDto.Response update(@PathParam("id") UUID id,
                                        @Valid IngatlanDto.Request dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Ingatlan törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
