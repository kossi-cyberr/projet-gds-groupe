package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.models.MvtStk;
import com.example.versuion.models.TypeMvtStk;
import com.example.versuion.repository.MvtStkRepository;
import com.example.versuion.services.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MvtStkServiceImplTest {

    @Mock
    private MvtStkRepository repository;
    @Mock
    private ArticleService articleService;

    private MvtStkServiceImpl mvtStkService;

    @BeforeEach
    void setUp() {
        mvtStkService = new MvtStkServiceImpl(repository, articleService);
    }

    private MvtStkDto validDto(BigDecimal quantite) {
        return MvtStkDto.builder()
                .dateMvt(Instant.now())
                .quantite(quantite)
                .article(ArticleDto.builder().id(1L).build())
                .typeMvt(TypeMvtStk.ENTREE)
                .build();
    }

    @Test
    void entreeStock_shouldForcePositiveQuantity() {
        when(repository.save(any(MvtStk.class))).thenAnswer(inv -> inv.getArgument(0));

        MvtStkDto result = mvtStkService.entreeStock(validDto(BigDecimal.valueOf(-5)));

        assertEquals(0, BigDecimal.valueOf(5).compareTo(result.getQuantite()));
        assertEquals(TypeMvtStk.ENTREE, result.getTypeMvt());
    }

    @Test
    void sortieStock_shouldForceNegativeQuantity() {
        when(repository.save(any(MvtStk.class))).thenAnswer(inv -> inv.getArgument(0));

        MvtStkDto result = mvtStkService.sortieStock(validDto(BigDecimal.valueOf(7)));

        assertEquals(0, BigDecimal.valueOf(-7).compareTo(result.getQuantite()));
        assertEquals(TypeMvtStk.SORTIE, result.getTypeMvt());
    }

    @Test
    void stockReelArticle_shouldReturnRepositoryValue() {
        when(repository.stockReelArticleTenant(1L)).thenReturn(BigDecimal.valueOf(12.5));

        BigDecimal result = mvtStkService.stockReelArticle(1L);

        assertEquals(0, BigDecimal.valueOf(12.5).compareTo(result));
    }

    @Test
    void stockReelArticle_shouldReturnMinusOneWhenIdNull() {
        BigDecimal result = mvtStkService.stockReelArticle(null);

        assertEquals(0, BigDecimal.valueOf(-1).compareTo(result));
        verify(repository, never()).stockReelArticleTenant(any());
        verify(articleService, never()).findById(any());
    }
}
