package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.SubjectRequest;
import com.rental_api.ServiceBooking.Dto.Response.SubjectResponse;
import com.rental_api.ServiceBooking.Services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // ================= CREATE =================
    @PostMapping
    public SubjectResponse create(@RequestBody SubjectRequest request) {
        return subjectService.create(request);
    }

    // ================= GET ALL =================
    @GetMapping
    public List<SubjectResponse> getAll() {
        return subjectService.getAll();
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public SubjectResponse getById(@PathVariable Long id) {
        return subjectService.getById(id);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public SubjectResponse update(
            @PathVariable Long id,
            @RequestBody SubjectRequest request) {
        return subjectService.update(id, request);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        subjectService.delete(id);
    }
}