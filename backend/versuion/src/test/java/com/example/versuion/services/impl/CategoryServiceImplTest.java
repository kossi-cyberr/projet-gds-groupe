package com.example.versuion.services.impl;

import com.example.versuion.Dto.CategoryDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.models.Category;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.CategoryRepository;
import com.example.versuion.utiles.CurrentEntreprise;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void save_shouldSaveCategoryWithSuccess() {
        CategoryDto expectedCategoryDto = CategoryDto.builder()
                .codeCategory("Mon")
                .designation("Mon Desognation")
                .idEntreprise(1)
                .build();

        Category category = new Category();
        category.setId(5L);
        category.setCodeCategory("Mon");
        category.setDesignation("Mon Desognation");
        category.setIdEntreprise(1);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto savedCategory = categoryService.save(expectedCategoryDto);

        assertNotNull(savedCategory);
        assertNotNull(savedCategory.getId());
        assertEquals("Mon", savedCategory.getCodeCategory());
        assertEquals("Mon Desognation", savedCategory.getDesignation());
        assertEquals(1, savedCategory.getIdEntreprise());
    }

    @Test
    void findById_shouldThrowEntityNotFoundExceptionWhenAbsent() {
        when(categoryRepository.findByIdTenant(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.findById(99L));
    }

    @Test
    void findAll_shouldFilterByEntrepriseWhenMdcPresent() {
        MDC.put(CurrentEntreprise.MDC_ID_ENTREPRISE, "2");
        try {
            assertEquals(2, CurrentEntreprise.getId());

            Category category = new Category();
            category.setId(1L);
            category.setCodeCategory("CAT");
            category.setIdEntreprise(2);
            when(categoryRepository.findAllTenant()).thenReturn(Collections.singletonList(category));

            List<CategoryDto> result = categoryService.findAll();

            assertEquals(1, result.size());
            assertEquals("CAT", result.get(0).getCodeCategory());
            assertEquals(2, result.get(0).getIdEntreprise());
        } finally {
            MDC.remove(CurrentEntreprise.MDC_ID_ENTREPRISE);
        }
    }

    @Test
    void currentEntreprise_shouldReturnNullWhenMdcEmpty() {
        MDC.remove(CurrentEntreprise.MDC_ID_ENTREPRISE);
        assertNull(CurrentEntreprise.getId());
    }

    @Test
    void delete_shouldDeleteCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setCodeCategory("CAT");
        when(categoryRepository.findByIdTenant(1L)).thenReturn(Optional.of(category));
        when(articleRepository.findAllByCategoryIdTenant(1L)).thenReturn(Collections.emptyList());

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenCategoryNotFound() {
        when(categoryRepository.findByIdTenant(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.delete(1L));
    }
}
