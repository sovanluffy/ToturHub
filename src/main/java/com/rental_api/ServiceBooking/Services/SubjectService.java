package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.SubjectRequest;
import com.rental_api.ServiceBooking.Dto.Response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse create(SubjectRequest request);

    List<SubjectResponse> getAll();

    SubjectResponse getById(Long id);

    SubjectResponse update(Long id, SubjectRequest request);

    void delete(Long id);
}