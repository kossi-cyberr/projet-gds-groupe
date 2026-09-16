package com.example.versuion.controller.api;

import com.example.versuion.Dto.PageResponse;
import com.example.versuion.Dto.VentesDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.versuion.utiles.Constants.VENTES_ENDPOINT;

@Tag(name = "Ventes", description = "Gestion des ventes")
public interface VentesApi {

    @PostMapping(VENTES_ENDPOINT + "/create")
    VentesDto save(@RequestBody VentesDto dto);

    @GetMapping(VENTES_ENDPOINT + "/{idVente}")
    VentesDto findById(@PathVariable("idVente") Long id);

    @GetMapping(VENTES_ENDPOINT + "/filter/{codeVente}")
    VentesDto findByCode(@PathVariable("codeVente") String code);

    @GetMapping(VENTES_ENDPOINT + "/all")
    List<VentesDto> findAll();

    @GetMapping(VENTES_ENDPOINT + "/paged")
    PageResponse<VentesDto> findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "id") String sortBy,
                                             @RequestParam(defaultValue = "asc") String sortDir,
                                             @RequestParam(required = false) String search);

    @DeleteMapping(VENTES_ENDPOINT + "/delete/{idVente}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    void delete(@PathVariable("idVente") Long id);
}
