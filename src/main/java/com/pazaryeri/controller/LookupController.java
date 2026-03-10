package com.pazaryeri.controller;

import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.entity.Category;
import com.pazaryeri.entity.City;
import com.pazaryeri.repository.CategoryRepository;
import com.pazaryeri.repository.CityRepository;
import com.pazaryeri.repository.DistrictRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LookupController {

    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    @GetMapping("/categories")
    @Tag(name = "Kategoriler")
    @Operation(summary = "Ana kategoriler")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        List<Category> categories = categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/categories/{id}/children")
    @Tag(name = "Kategoriler")
    @Operation(summary = "Alt kategoriler")
    public ResponseEntity<ApiResponse<List<Category>>> getSubCategories(@PathVariable Long id) {
        List<Category> subs = categoryRepository.findByParentIdAndActiveTrueOrderBySortOrderAsc(id);
        return ResponseEntity.ok(ApiResponse.success(subs));
    }

    @GetMapping("/cities")
    @Tag(name = "Konum")
    @Operation(summary = "Şehirler listesi")
    public ResponseEntity<ApiResponse<List<City>>> getCities() {
        return ResponseEntity.ok(ApiResponse.success(cityRepository.findAllByOrderByNameAsc()));
    }

    @GetMapping("/cities/{cityId}/districts")
    @Tag(name = "Konum")
    @Operation(summary = "Şehre göre ilçeler")
    public ResponseEntity<?> getDistricts(@PathVariable Long cityId) {
        return ResponseEntity.ok(ApiResponse.success(districtRepository.findByCityIdOrderByNameAsc(cityId)));
    }
}
