package com.example.versuion.services;

import com.example.versuion.Dto.ChangerMotDePasseUtilisateurDto;
import com.example.versuion.Dto.UtilisateurDto;

import java.util.List;

public interface UtilisateurService {

    UtilisateurDto save(UtilisateurDto dto);

    UtilisateurDto findById(Long id);

    List<UtilisateurDto> findAll();

    void delete(Long id);

    UtilisateurDto findByEmail(String email);

    UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto);

    /**
     * Affecte un rôle (ADMIN / MANAGER / VENDEUR) à un utilisateur de l'entreprise courante.
     */
    UtilisateurDto assignerRole(Long idUtilisateur, String roleName);

    /**
     * Met à jour le profil de l'utilisateur connecté (nom, prénom, date de naissance,
     * adresse, photo). L'email et le mot de passe ne sont pas modifiables ici.
     */
    UtilisateurDto updateMonProfil(UtilisateurDto dto);

    /**
     * Enregistre la photo de profil de l'utilisateur connecté et renvoie son URL.
     */
    UtilisateurDto updateMaPhoto(String urlPhoto);
}
