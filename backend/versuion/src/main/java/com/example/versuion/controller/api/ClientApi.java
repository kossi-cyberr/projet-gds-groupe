package com.example.versuion.controller.api;

import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.versuion.utiles.Constants.APP_ROOT;
@Tag(name = "Clients", description = "Gestion des clients")
public interface ClientApi {

    @PostMapping(value = APP_ROOT + "/clients/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ClientDto save(@RequestBody ClientDto dto);
    //Objet de type Json puis il va faire le mapping nécessaire yomkon mabin el json wel dto

    @PutMapping(value = APP_ROOT + "/clients/update/{idClient}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ClientDto update(@PathVariable("idClient") Long id, @RequestBody ClientDto dto);

    @GetMapping(value = APP_ROOT + "/clients/{idClient}", produces = MediaType.APPLICATION_JSON_VALUE)
    ClientDto findById(@PathVariable("idClient") Long id);

    @GetMapping(value = APP_ROOT + "/clients/all", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ClientDto> findAll();

    @GetMapping(value = APP_ROOT + "/clients/paged", produces = MediaType.APPLICATION_JSON_VALUE)
    PageResponse<ClientDto> findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "id") String sortBy,
                                             @RequestParam(defaultValue = "asc") String sortDir,
                                             @RequestParam(required = false) String search);

    @DeleteMapping(value = APP_ROOT + "/clients/delete/{idClient}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    void delete(@PathVariable("idClient") Long id);
}
