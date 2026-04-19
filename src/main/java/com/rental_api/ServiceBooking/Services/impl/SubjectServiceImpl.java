package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.SubjectRequest;
import com.rental_api.ServiceBooking.Dto.Response.SubjectResponse;
import com.rental_api.ServiceBooking.Entity.Subject;
import com.rental_api.ServiceBooking.Repository.SubjectRepository;
import com.rental_api.ServiceBooking.Services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    // ================= CREATE =================
    @Override
    public SubjectResponse create(SubjectRequest request) {

        if (subjectRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subject already exists");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .build();

        Subject saved = subjectRepository.save(subject);

        return mapToResponse(saved);
    }

    // ================= GET ALL =================
    @Override
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= GET BY ID =================
    @Override
    public SubjectResponse getById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        return mapToResponse(subject);
    }

    // ================= UPDATE =================
    @Override
    public SubjectResponse update(Long id, SubjectRequest request) {

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        subject.setName(request.getName());

        return mapToResponse(subjectRepository.save(subject));
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {
        subjectRepository.deleteById(id);
    }

    // ================= MAPPER =================
    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }
}