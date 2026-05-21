package hu.ingatlan.rest;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof NotFoundException) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        }
        if (e instanceof WebApplicationException wae) {
            return error(Response.Status.fromStatusCode(wae.getResponse().getStatus()),
                    wae.getMessage());
        }
        if (e instanceof ConstraintViolationException cve) {
            String details = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return error(Response.Status.BAD_REQUEST, "Validációs hiba: " + details);
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR,
                "Belső szerverhiba: " + e.getMessage());
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", status.getStatusCode(),
                        "hiba", message,
                        "idopont", LocalDateTime.now().toString()
                ))
                .build();
    }
}
