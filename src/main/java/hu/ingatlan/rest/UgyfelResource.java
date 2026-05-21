package hu.ingatlan.rest;

import hu.ingatlan.dto.UgyfelDto;
import hu.ingatlan.service.UgyfelService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/ugyfelek")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ügyfelek", description = "Ügyfélkezelés")
public class UgyfelResource {

    @Inject
    UgyfelService service;

    @GET
    @Operation(summary = "Összes ügyfél listázása")
    public List<UgyfelDto.Response> list() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Ügyfél lekérdezése ID alapján")
    public UgyfelDto.Response get(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Új ügyfél létrehozása")
    public Response create(@Valid UgyfelDto.Request dto) {
        UgyfelDto.Response created = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Ügyfél módosítása")
    public UgyfelDto.Response update(@PathParam("id") UUID id,
                                      @Valid UgyfelDto.Request dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Ügyfél törlése")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
