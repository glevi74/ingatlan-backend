package hu.ingatlan.rest;

import hu.ingatlan.dto.HirDto;
import hu.ingatlan.service.HirService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Path("/api/v1/hirek")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Hírek")
public class HirResource {

    @Inject
    HirService service;

    @GET
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Legutóbbi hírek listája (max 30 nap)")
    public List<HirDto.Response> list() {
        return service.listRecent();
    }

    @GET
    @Path("/{datum}")
    @RolesAllowed({"ADMIN", "IRODAVEZETO", "REFERENS", "ASSZISZTENS"})
    @Operation(summary = "Adott napra vonatkozó hírösszefoglaló lekérdezése (pl. 2024-01-15)")
    public HirDto.Response getByDatum(@PathParam("datum") String datumStr) {
        LocalDate datum;
        try {
            datum = LocalDate.parse(datumStr);
        } catch (Exception e) {
            throw new BadRequestException("Érvénytelen dátumformátum – ISO-8601 szükséges (pl. 2024-01-15)");
        }
        return service.findByDatum(datum);
    }

    @POST
    @Path("/generalt")
    @RolesAllowed({"ADMIN", "IRODAVEZETO"})
    @Operation(summary = "Kézi hírgenerálás adott napra (ADMIN / IRODAVEZETŐ)")
    public Response generate(HirDto.GeneralasRequest req) {
        LocalDate datum = (req != null && req.getDatum() != null)
                ? req.getDatum()
                : LocalDate.now().minusDays(1);
        HirDto.Response result = service.generateForDate(datum);
        return Response.ok(result).build();
    }
}
