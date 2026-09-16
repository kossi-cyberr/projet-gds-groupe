package com.example.versuion.Dto;

import com.example.versuion.models.Adresse;
import com.example.versuion.models.Entreprise;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import jakarta.persistence.Embedded;
import jakarta.persistence.OneToMany;
import java.util.List;
@Builder
@Data
public class EntrepriseDto {

    private Long id;

    private String nom;

    private String description;

    private AdressDto adresse;

    private String codefiscale;

    private String photo;

    private String email;

    private String numTel;

    private String siteWeb;

    /**
     * Mot de passe temporaire de l'administrateur : renseigné UNIQUEMENT dans la
     * réponse de la création d'entreprise. Absent (null) ailleurs.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String motDePasse;

    @JsonIgnore
    private List<UtilisateurDto> utilisateurs;

    //Mapping
    public static EntrepriseDto fromEntity(Entreprise entreprise){
        if(entreprise == null){
            return null;
        }
        return EntrepriseDto.builder()
                .id(entreprise.getId())
                .nom(entreprise.getNom())
                .description(entreprise.getDescription())
                .adresse(AdressDto.fromEntity(entreprise.getAdresse()))
                .codefiscale(entreprise.getCodefiscale())
                .photo(entreprise.getPhoto())
                .email(entreprise.getEmail())
                .numTel(entreprise.getNumTel())
                .siteWeb(entreprise.getSiteWeb())
                .build();
    }

    public static Entreprise toEntity(EntrepriseDto entrepriseDto){
        if(entrepriseDto == null){
            return null;
        }
        Entreprise entreprise = new Entreprise();
        entreprise.setId(entrepriseDto.getId());
        entreprise.setNom(entrepriseDto.getNom());
        entreprise.setDescription(entrepriseDto.getDescription());
        entreprise.setAdresse(AdressDto.toEntity(entrepriseDto.getAdresse()));
        entreprise.setCodefiscale(entrepriseDto.getCodefiscale());
        entreprise.setPhoto(entrepriseDto.getPhoto());
        entreprise.setEmail(entrepriseDto.getEmail());
        entreprise.setNumTel(entrepriseDto.getNumTel());
        entreprise.setSiteWeb(entrepriseDto.getSiteWeb());

        return entreprise;
    }
}

