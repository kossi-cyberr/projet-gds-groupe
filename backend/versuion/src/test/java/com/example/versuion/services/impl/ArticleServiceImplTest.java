package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.Category;
import com.example.versuion.models.LigneVente;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.CategoryRepository;
import com.example.versuion.repository.LigneCommandeClientRepository;
import com.example.versuion.repository.LigneCommandeFournisseurRepository;
import com.example.versuion.repository.LigneVenteRepository;
import com.example.versuion.utiles.CurrentEntreprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private LigneVenteRepository ligneVenteRepository;
    @Mock
    private LigneCommandeClientRepository ligneCommandeClientRepository;
    @Mock
    private LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article article;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(10L);
        category.setCodeCategory("CAT");

        article = new Article();
        article.setId(1L);
        article.setCodeArticle("ART001");
        article.setDesignation("Produit test");
        article.setPrixUnitaire(BigDecimal.valueOf(100));
        article.setPrixUnitaireTTc(BigDecimal.valueOf(118));
        article.setCategory(category);
    }

    @Test
    void findById_shouldReturnArticleWhenExists() {
        when(articleRepository.findByIdTenant(1L)).thenReturn(Optional.of(article));

        ArticleDto result = articleService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("ART001", result.getCodeArticle());
        assertEquals("Produit test", result.getDesignation());
    }

    @Test
    void findById_shouldThrowEntityNotFoundExceptionWhenAbsent() {
        when(articleRepository.findByIdTenant(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.findById(99L));
    }

    @Test
    void findByCodeArticle_shouldThrowEntityNotFoundExceptionWhenAbsent() {
        when(articleRepository.findByCodeArticleTenant("INCONNU")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.findByCodeArticle("INCONNU"));
    }

    @Test
    void save_shouldThrowInvalidEntityExceptionWhenDtoInvalid() {
        ArticleDto invalid = ArticleDto.builder().build();

        assertThrows(InvalidEntityException.class, () -> articleService.save(invalid));
    }

    @Test
    void save_shouldThrowEntityNotFoundExceptionWhenCategoryAbsent() {
        when(categoryRepository.findByIdTenant(10L)).thenReturn(Optional.empty());

        ArticleDto dto = ArticleDto.builder()
                .codeArticle("ART001")
                .designation("Produit test")
                .prixUnitaire(BigDecimal.valueOf(100))
                .tauxTva(BigDecimal.valueOf(18))
                .prixUnitaireTTc(BigDecimal.valueOf(118))
                .category(com.example.versuion.Dto.CategoryDto.builder().id(10L).build())
                .build();

        assertThrows(EntityNotFoundException.class, () -> articleService.save(dto));
    }

    @Test
    void save_shouldForceCurrentEntrepriseWhenMdcPresent() {
        MDC.put(CurrentEntreprise.MDC_ID_ENTREPRISE, "5");
        try {
            when(categoryRepository.findByIdTenant(10L)).thenReturn(Optional.of(category));
            when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

            ArticleDto dto = ArticleDto.builder()
                    .codeArticle("ART001")
                    .designation("Produit test")
                    .prixUnitaire(BigDecimal.valueOf(100))
                    .tauxTva(BigDecimal.valueOf(18))
                    .prixUnitaireTTc(BigDecimal.valueOf(118))
                    .category(com.example.versuion.Dto.CategoryDto.builder().id(10L).build())
                    .build();

            ArticleDto result = articleService.save(dto);

            assertEquals(5, result.getIdEntreprise());
        } finally {
            MDC.remove(CurrentEntreprise.MDC_ID_ENTREPRISE);
        }
    }

    @Test
    void delete_shouldThrowInvalidOperationExceptionWhenArticleUsedInVentes() {
        when(articleRepository.findByIdTenant(1L)).thenReturn(Optional.of(article));
        LigneVente ligneVente = new LigneVente();
        ligneVente.setId(1L);
        when(ligneVenteRepository.findAllByArticleIdTenant(1L)).thenReturn(Collections.singletonList(ligneVente));

        assertThrows(InvalidOperationException.class, () -> articleService.delete(1L));
    }

    @Test
    void delete_shouldDeleteArticleWhenNotUsed() {
        when(articleRepository.findByIdTenant(1L)).thenReturn(Optional.of(article));
        when(ligneCommandeClientRepository.findAllByArticleIdTenant(1L)).thenReturn(Collections.emptyList());
        when(ligneCommandeFournisseurRepository.findAllByArticleIdTenant(1L)).thenReturn(Collections.emptyList());
        when(ligneVenteRepository.findAllByArticleIdTenant(1L)).thenReturn(Collections.emptyList());

        articleService.delete(1L);

        verify(articleRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenArticleBelongsToAnotherEntreprise() {
        when(articleRepository.findByIdTenant(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> articleService.delete(1L));
    }
}
