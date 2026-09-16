package com.example.versuion.controller.api;

import com.example.versuion.Dto.ChangerMotDePasseUtilisateurDto;
import com.example.versuion.Dto.UtilisateurDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.versuion.utiles.Constants.UTILISATEUR_ENDPOINT;
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public interface UtilisateurApi {

    @PostMapping(UTILISATEUR_ENDPOINT + "/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    UtilisateurDto save(@RequestBody UtilisateurDto dto);

    @GetMapping(UTILISATEUR_ENDPOINT + "/{idUtilisateur}")
    UtilisateurDto findById(@PathVariable("idUtilisateur") Long id);

    @GetMapping(UTILISATEUR_ENDPOINT + "/find/{email}")
    UtilisateurDto findByEmail(@PathVariable("email") String email);

    @GetMapping(UTILISATEUR_ENDPOINT + "/all")
    List<UtilisateurDto> findAll();

    @DeleteMapping(UTILISATEUR_ENDPOINT + "/delete/{idUtilisateur}")
    @PreAuthorize("hasAuthority('ADMIN')")
    void delete(@PathVariable("idUtilisateur") Long id);

    @PostMapping(UTILISATEUR_ENDPOINT + "/update/password")
    UtilisateurDto changerMotDePasse(@RequestBody ChangerMotDePasseUtilisateurDto dto);

    @PutMapping(UTILISATEUR_ENDPOINT + "/roles/{idUtilisateur}/{roleName}")
    @PreAuthorize("hasAuthority('ADMIN')")
    UtilisateurDto assignerRole(@PathVariable("idUtilisateur") Long idUtilisateur, @PathVariable("roleName") String roleName);

    @PutMapping(UTILISATEUR_ENDPOINT + "/me")
    UtilisateurDto updateMonProfil(@RequestBody UtilisateurDto dto);

    @PostMapping(UTILISATEUR_ENDPOINT + "/me/photo")
    UtilisateurDto updateMaPhoto(@RequestParam("file") MultipartFile fichier);

}
