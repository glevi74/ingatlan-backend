package hu.ingatlan.rest;

import hu.ingatlan.dto.AuthDto;
import hu.ingatlan.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autentikáció", description = "Bejelentkezés és felhasználókezelés")
public class AuthResource {

    @Inject
    AuthService service;

    @POST
    @Path("/bejelentkezes")
    @Operation(summary = "Bejelentkezés — JWT tokent ad vissza")
    public AuthDto.TokenResponse bejelentkezes(@Valid AuthDto.BejelentkezesRequest req) {
        return service.bejelentkezes(req);
    }

    @POST
    @Path("/regisztracio")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Új felhasználó regisztrálása (csak ADMIN)")
    public Response regisztracio(@Valid AuthDto.RegisztracioRequest req) {
        service.regisztracio(req);
        return Response.status(Response.Status.CREATED).build();
    }
}
