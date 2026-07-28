package com.josemaba.marquesitasapi.mapper;

import com.josemaba.marquesitasapi.dto.request.CategoryRequest;
import com.josemaba.marquesitasapi.dto.response.CategoryResponse;
import com.josemaba.marquesitasapi.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
