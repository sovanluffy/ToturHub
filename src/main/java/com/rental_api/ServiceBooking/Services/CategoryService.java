package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.CategoryRequest;
import com.rental_api.ServiceBooking.Dto.Response.CategoryResponse;
import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest categoryRequest);

    List<CategoryResponse> getAll();

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
