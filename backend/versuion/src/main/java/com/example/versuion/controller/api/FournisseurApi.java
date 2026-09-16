package com.example.versuion.controller.api;

import com.example.versuion.Dto.FournisseurDto;
import com.example.versuion.Dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.versuion.utiles.Constants.FOURNISSEUR_ENDPOINT;

@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs")
public interface FournisseurApi {

    @PostMapping(FOURNISSEUR_ENDPOINT + "/create")
    FournisseurDto save(@RequestBody FournisseurDto dto);

    @PutMapping(FOURNISSEUR_ENDPOINT + "/update/{idFournisseur}")
    FournisseurDto update(@PathVariable("idFournisseur") Long id, @RequestBody FournisseurDto dto);

    @GetMapping(FOURNISSEUR_ENDPOINT + "/{idFournisseur}")
    FournisseurDto findById(@PathVariable("idFournisseur") Long id);

    @GetMapping(FOURNISSEUR_ENDPOINT + "/all")
    List<FournisseurDto> findAll();

    @GetMapping(FOURNISSEUR_ENDPOINT + "/paged")
    PageResponse<FournisseurDto> findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "id") String sortBy,
                                                  @RequestParam(defaultValue = "asc") String sortDir,
                                                  @RequestParam(required = false) String search);

    @DeleteMapping(FOURNISSEUR_ENDPOINT + "/delete/{idFournisseur}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    void delete(@PathVariable("idFournisseur") Long id);
}
