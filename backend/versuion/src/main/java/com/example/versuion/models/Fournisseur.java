package com.example.versuion.models;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Fournisseur extends AbstractEntity{

    private String nom;

    private String prenom;

    @Embedded
    private Adresse adresse;

    private String photo;

    private String mail;

    private String numTel;

    @Column(name = "identreprise")
    private Integer idEntreprise;

    @OneToMany(mappedBy = "Fournisseur")
    private List<ComandeFournisseur> comandeFournisseurList;
}
