package com.example.versuion.models;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class LigneVente extends AbstractEntity{

    @ManyToOne
    @JoinColumn(name = "idVent")
    private Ventes vente;

    private BigDecimal quantie;

    @ManyToOne
    @JoinColumn(name = "idarticle")
    private Article article;

    private BigDecimal prixUnitare;

    @Column(name = "identreprise")
    private Integer idEntreprise;
}
