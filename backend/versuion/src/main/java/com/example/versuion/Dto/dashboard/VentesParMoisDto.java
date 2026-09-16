package com.example.versuion.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Projection JPQL : chiffre d'affaires par mois (pour la courbe annuelle).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentesParMoisDto {

    private Integer annee;

    private Integer mois;

    private BigDecimal chiffreAffaires;
}
