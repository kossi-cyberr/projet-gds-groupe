package com.example.versuion.controller.api;

import com.example.versuion.Dto.EntrepriseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.versuion.utiles.Constants.ENTREPRISE_ENDPOINT;
@Tag(name = "Entreprises", description = "Gestion des entreprises")
public interface EntrepriseApi {

    @PostMapping(ENTREPRISE_ENDPOINT + "/create")
    EntrepriseDto save(@RequestBody EntrepriseDto dto);

    @GetMapping(ENTREPRISE_ENDPOINT + "/{idEntreprise}")
    @PreAuthorize("hasAuthority('ADMIN')")
    EntrepriseDto findById(@PathVariable("idEntreprise") Long id);

    @GetMapping(ENTREPRISE_ENDPOINT + "/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    List<EntrepriseDto> findAll();

    @DeleteMapping(ENTREPRISE_ENDPOINT + "/delete/{idEntreprise}")
    @PreAuthorize("hasAuthority('ADMIN')")
    void delete(@PathVariable("idEntreprise") Long id);

    @GetMapping("/message")
    public String getMessage();
}
