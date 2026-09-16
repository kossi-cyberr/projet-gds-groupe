package com.example.versuion.controller;

import com.example.versuion.Dto.ChangerMotDePasseUtilisateurDto;
import com.example.versuion.Dto.UtilisateurDto;
import com.example.versuion.controller.api.UtilisateurApi;
import com.example.versuion.services.PhotoStorageService;
import com.example.versuion.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class UtilisateurControlleur implements UtilisateurApi {

    UtilisateurService utilisateurService;
    PhotoStorageService photoStorageService;

    @Autowired
    public UtilisateurControlleur(UtilisateurService utilisateurService, PhotoStorageService photoStorageService){
        this.utilisateurService = utilisateurService;
        this.photoStorageService = photoStorageService;
    }

    @Override
    public UtilisateurDto save(UtilisateurDto dto) {
        return utilisateurService.save(dto);
    }

    @Override
    public UtilisateurDto findById(Long id) {
        return utilisateurService.findById(id);
    }

    @Override
    public UtilisateurDto findByEmail(String email) {
        return utilisateurService.findByEmail(email);
    }

    @Override
    public List<UtilisateurDto> findAll() {
        return utilisateurService.findAll();
    }

    @Override
    public void delete(Long id) {
        utilisateurService.delete(id);
    }
    @Override
    public UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto) {
        return utilisateurService.changerMotDePasse(dto);
    }

    @Override
    public UtilisateurDto assignerRole(Long idUtilisateur, String roleName) {
        return utilisateurService.assignerRole(idUtilisateur, roleName);
    }

    @Override
    public UtilisateurDto updateMonProfil(UtilisateurDto dto) {
        return utilisateurService.updateMonProfil(dto);
    }

    @Override
    public UtilisateurDto updateMaPhoto(MultipartFile fichier) {
        String url = photoStorageService.enregistrer(fichier);
        return utilisateurService.updateMaPhoto(url);
    }
}
