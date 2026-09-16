package com.example.versuion.models;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Roles extends AbstractEntity{

    private String rolename;

    @ManyToOne
    @JoinColumn(name = "idUtilisateur")
    private Utilisateurs utilisateur;

}
