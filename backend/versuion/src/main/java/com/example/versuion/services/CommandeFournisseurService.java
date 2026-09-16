package com.example.versuion.services;

import com.example.versuion.Dto.ComandeFournisseurDto;
import com.example.versuion.Dto.LigneCommandeFournisseurDto;
import com.example.versuion.models.EtatCommande;

import java.math.BigDecimal;
import java.util.List;

public interface CommandeFournisseurService {

    ComandeFournisseurDto save(ComandeFournisseurDto dto);

    ComandeFournisseurDto findById(Long id);

    ComandeFournisseurDto findByCode(String code);

    List<ComandeFournisseurDto> findAll();

    void delete(Long id);

    ComandeFournisseurDto updateEtatCommande(Long idCommande, EtatCommande etatCommande);

    ComandeFournisseurDto updateQuantiteCommande(Long idCommande, Long idLigneCommande, BigDecimal quantite);

    ComandeFournisseurDto updateFournisseur(Long idCommande, Long idFournisseur);

    ComandeFournisseurDto updateArticle(Long idCommande, Long idLigneCommande, Long idArticle);

    ComandeFournisseurDto deleteArticle(Long idCommande, Long idLigneCommande);

    List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(Long idCommande);
}
