package com.example.versuion.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Projection JPQL : commandes et montants par client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandesParClientDto {

    private Long idClient;

    private String nom;

    private String prenom;

    private Long nombreCommandes;

    private BigDecimal montantTotal;
}
