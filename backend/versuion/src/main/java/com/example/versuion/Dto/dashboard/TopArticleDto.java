package com.example.versuion.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Projection JPQL : les articles les plus vendus (top 10).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopArticleDto {

    private Long idArticle;

    private String designation;

    private String codeArticle;

    private BigDecimal quantiteVendue;

    private BigDecimal chiffreAffaires;
}
