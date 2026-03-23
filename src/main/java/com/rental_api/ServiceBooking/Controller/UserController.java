// package com.rental_api.ServiceBooking.Controller;

// import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
// import com.rental_api.ServiceBooking.Dto.Response.UserResponse;
// import com.rental_api.ServiceBooking.Services.UserService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/users")
// @RequiredArgsConstructor
// @Tag(name = "User", description = "User management APIs")
// public class UserController {

//     private final UserService userService;

//     // ---------------- GET ALL USERS ----------------
//     @GetMapping
//     @Operation(summary = "Get all users", description = "Returns all users")
//     public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
//         List<UserResponse> users = userService.getAllUsers();

//         ApiResponse<List<UserResponse>> response = new ApiResponse<>();
//         response.setStatus(200);
//         response.setMessage("All users fetched successfully");
//         response.setData(users);

//         return ResponseEntity.ok(response);
//     }

//     // ---------------- GET USER BY ID ----------------
//     @GetMapping("/{id}")
//     @Operation(summary = "Get user by ID", description = "Returns user details by ID")
//     public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
//         UserResponse user = userService.getUserById(id);

//         ApiResponse<UserResponse> response = new ApiResponse<>();
//         response.setStatus(200);
//         response.setMessage("User fetched successfully");
//         response.setData(user);

//         return ResponseEntity.ok(response);
//     }

   
//     // ---------------- UPDATE USER ----------------
//     @PutMapping("/{id}")
//     @Operation(summary = "Update a user", description = "Updates user details by ID")
//     public ResponseEntity<ApiResponse<UserResponse>> updateUser(
//             @PathVariable Long id,
//             @RequestBody UserResponse updatedData
//     ) {
//         UserResponse updatedUser = userService.updateUser(id, updatedData);

//         ApiResponse<UserResponse> response = new ApiResponse<>();
//         response.setStatus(200);
//         response.setMessage("User updated successfully");
//         response.setData(updatedUser);

//         return ResponseEntity.ok(response);
//     }

//     // ---------------- DELETE USER ----------------
//     @DeleteMapping("/{id}")
//     @Operation(summary = "Delete a user", description = "Deletes a user by ID")
//     public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
//         userService.deleteUser(id);

//         ApiResponse<Void> response = new ApiResponse<>();
//         response.setStatus(200);
//         response.setMessage("User deleted successfully");

//         return ResponseEntity.ok(response);
//     }
// }
