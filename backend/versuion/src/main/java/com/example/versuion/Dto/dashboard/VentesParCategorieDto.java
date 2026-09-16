package com.example.versuion.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Projection JPQL : chiffre d'affaires par catégorie d'articles.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentesParCategorieDto {

    private Long idCategory;

    private String designation;

    private BigDecimal chiffreAffaires;
}
