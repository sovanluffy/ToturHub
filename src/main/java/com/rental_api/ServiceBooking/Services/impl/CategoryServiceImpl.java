package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.CategoryRequest;
import com.rental_api.ServiceBooking.Dto.Response.CategoryResponse;
import com.rental_api.ServiceBooking.Entity.Category;
import com.rental_api.ServiceBooking.Exception.ConflictException;
import com.rental_api.ServiceBooking.Exception.RequestNotFoundException;
import com.rental_api.ServiceBooking.Repository.CategoryRepository;
import com.rental_api.ServiceBooking.Services.CategoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.rental_api.ServiceBooking.Exception.CategoryNotFoundException;


import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public CategoryResponse create(CategoryRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RequestNotFoundException("Category name is required");
        }

        if (repository.existsByName(request.getName())) {
            throw new ConflictException("Category name already exists");
        }

          

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category savedCategory = repository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    public List<CategoryResponse> getAll() {
        if (repository.count() == 0) {
            throw new CategoryNotFoundException("No categories found");
        }
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

   
    @Override
    public CategoryResponse getById(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
        "Category not found with id: " + id
        ));
        return mapToResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
        "Category not found with id: " + id
        ));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = repository.save(category);
        return mapToResponse(updatedCategory);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
    return new CategoryResponse(
        category.getId(),
        category.getName(),
        category.getDescription() // if you added description
    );
}
}
