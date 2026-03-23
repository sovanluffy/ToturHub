package com.rental_api.ServiceBooking.Exception;

import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ---------------- 404 NOT FOUND ----------------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> userNotFound(UserNotFoundException ex) {
        return buildResponse(404, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> roleNotFound(RoleNotFoundException ex) {
        return buildResponse(404, "Role Not Found", ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> categoryNotFound(CategoryNotFoundException ex) {
        return buildResponse(404, "Category Not Found", ex.getMessage());
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> serviceNotFound(ServiceNotFoundException ex) {
        return buildResponse(404, "Service Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> endpointNotFound(NoHandlerFoundException ex) {
        return buildResponse(404, "Endpoint Not Found", "The requested endpoint does not exist.");
    }

    // ---------------- 401 UNAUTHORIZED ----------------

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> unauthorized(Exception ex) {
        return buildResponse(401, "Unauthorized", ex.getMessage());
    }

    // ---------------- 409 CONFLICT ----------------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> conflict(ConflictException ex) {
        return buildResponse(409, "Conflict", ex.getMessage());
    }

    // ---------------- 400 BAD REQUEST ----------------

    @ExceptionHandler({InvalidInputException.class, RuntimeException.class})
    public ResponseEntity<ApiResponse<Object>> badRequest(Exception ex) {
        return buildResponse(400, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> typeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Invalid value for parameter '%s'. Expected type: %s.", paramName, requiredType);
        return buildResponse(400, "Bad Request", message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Validation failed");
        return buildResponse(400, "Validation Error", message);
    }

    // ---------------- 500 INTERNAL SERVER ERROR ----------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAll(Exception ex) {
        return buildResponse(500, "Internal Server Error", ex.getMessage());
    }

    // ---------------- PRIVATE HELPER ----------------

    private ResponseEntity<ApiResponse<Object>> buildResponse(int status, String error, String message) {
        ApiResponse<Object> response = ApiResponse.error(status, error, message);
        return ResponseEntity.status(status).body(response);
    }
}
