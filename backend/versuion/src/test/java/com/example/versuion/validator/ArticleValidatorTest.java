package com.example.versuion.validator;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.CategoryDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleValidatorTest {

    private ArticleDto articleValide() {
        return ArticleDto.builder()
                .codeArticle("ND-CIM-050")
                .designation("Ciment CIMAF 50 kg")
                .prixUnitaire(BigDecimal.valueOf(4650))
                .tauxTva(BigDecimal.valueOf(19.25))
                .prixUnitaireTTc(BigDecimal.valueOf(5545))
                .category(CategoryDto.builder().id(1L).build())
                .build();
    }

    @Test
    void articleCompletEstValide() {
        assertTrue(ArticleValidator.validate(articleValide()).isEmpty());
    }

    @Test
    void articleNullEstInvalide() {
        List<String> errors = ArticleValidator.validate(null);
        assertEquals(6, errors.size());
    }

    @Test
    void sansCodeNiDesignationEstInvalide() {
        ArticleDto a = articleValide();
        a.setCodeArticle(null);
        a.setDesignation(null);
        List<String> errors = ArticleValidator.validate(a);
        assertEquals(2, errors.size());
    }

    @Test
    void sansCategorieEstInvalide() {
        ArticleDto a = articleValide();
        a.setCategory(null);
        List<String> errors = ArticleValidator.validate(a);
        assertTrue(errors.contains("Veuillez selectionner une categorie"));
    }

    @Test
    void sansPrixEstInvalide() {
        ArticleDto a = articleValide();
        a.setPrixUnitaire(null);
        a.setPrixUnitaireTTc(null);
        List<String> errors = ArticleValidator.validate(a);
        assertEquals(2, errors.size());
    }
}
