package com.example.versuion.services.impl;

import com.example.versuion.Dto.CategoryDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.Category;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.CategoryRepository;
import com.example.versuion.services.CategoryService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.validator.CategoryValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        List<String> errors = CategoryValidator.validate(categoryDto);
        if (!errors.isEmpty()) {
            log.error("Category is not valid {}", categoryDto);
            throw new InvalidEntityException("La category n'est pas valide", ErrorCodes.CATEGORY_NOT_VALID, errors);
        }
        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            categoryDto.setIdEntreprise(idEntreprise);
        }
        Category categoryEntity = CategoryDto.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(categoryEntity);
        return CategoryDto.fromEntity(savedCategory);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        log.info("Inside update category {}", id);
        Category category = categoryRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune category avec l'ID " + id + " n'a ete trouve dans la base",
                        ErrorCodes.CATEGORY_NOT_FOUND));
        log.info("Category found with id {}", id);
        category.setCodeCategory(categoryDto.getCodeCategory());
        category.setDesignation(categoryDto.getDesignation());
        category.setIdEntreprise(categoryDto.getIdEntreprise());
        categoryRepository.save(category);
        log.info("Category with id {} updated", id);
        return CategoryDto.fromEntity(category);
    }

    @Override
    public CategoryDto findById(Long id) {
        if (id == null) {
            log.error("Category ID is null");
            return null;
        }
        return categoryRepository.findByIdTenant(id)
                .map(CategoryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune category avec l'ID " + id + " n'a ete trouve dans la base",
                        ErrorCodes.CATEGORY_NOT_FOUND));
    }

    @Override
    public CategoryDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Category CODE is null");
            return null;
        }
        return categoryRepository.findByCodeCategoryTenant(code)
                .map(CategoryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune category avec le code " + code + " n'a ete trouve dans la base",
                        ErrorCodes.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAllTenant().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Category ID is null");
            return;
        }
        // Verifier que la categorie appartient bien a l'entreprise courante avant suppression
        categoryRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune category avec l'ID " + id + " n'a ete trouve dans la base",
                        ErrorCodes.CATEGORY_NOT_FOUND));
        List<Article> articles = articleRepository.findAllByCategoryIdTenant(id);
        if (!articles.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer cette categorie qui est deja utilise",
                    ErrorCodes.CATEGORY_ALREADY_IN_USE);
        }
        categoryRepository.deleteById(id);
    }
}
