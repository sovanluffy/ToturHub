package com.rental_api.ServiceBooking.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    // --- SUCCESS METHODS ---

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Operation successful", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // --- ERROR METHODS ---

    // Matches your GlobalExceptionHandler: .error(status, error, message)
    public static <T> ApiResponse<T> error(int status, String error, String message) {
        // We can pass the 'message' as the data or use a specific format
        return new ApiResponse<>(status, error, (T) message);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}