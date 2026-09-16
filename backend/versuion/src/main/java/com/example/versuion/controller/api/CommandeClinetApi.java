package com.example.versuion.controller.api;

import com.example.versuion.Dto.ComandeClientDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.models.EtatCommande;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.example.versuion.utiles.Constants.APP_ROOT;
@Tag(name = "Commandes clients", description = "Gestion des commandes clients")
public interface CommandeClinetApi {
    @PostMapping(APP_ROOT + "/commandesclients/create")
    ResponseEntity<ComandeClientDto> save(@RequestBody ComandeClientDto dto);

    @GetMapping(APP_ROOT + "/commandesclients/{idCommandeClient}")
    ResponseEntity<ComandeClientDto> findById(@PathVariable Long idCommandeClient);

    @GetMapping(APP_ROOT + "/commandesclients/filter/{codeCommandeClient}")
    ResponseEntity<ComandeClientDto> findByCode(@PathVariable("codeCommandeClient") String code);

    @GetMapping(APP_ROOT + "/commandesclients/all")
    ResponseEntity<List<ComandeClientDto>> findAll();

    @GetMapping(APP_ROOT + "/commandesclients/paged")
    ResponseEntity<PageResponse<ComandeClientDto>> findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                                    @RequestParam(defaultValue = "asc") String sortDir,
                                                                    @RequestParam(required = false) String search);

    @DeleteMapping(APP_ROOT + "/commandesclients/delete/{idCommandeClient}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    ResponseEntity delete(@PathVariable("idCommandeClient") Long id);


    @PatchMapping(APP_ROOT + "/commandesclients/update/etat/{idCommande}/{etatCommande}")
    ResponseEntity<ComandeClientDto> updateEtatCommande(@PathVariable("idCommande") Long idCommande, @PathVariable("etatCommande") EtatCommande etatCommande);

    @PatchMapping(APP_ROOT + "/commandesclients/update/quantite/{idCommande}/{idLigneCommande}/{quantite}")
    ResponseEntity<ComandeClientDto> updateQuantiteCommande(@PathVariable("idCommande") Long idCommande,
                                                             @PathVariable("idLigneCommande") Long idLigneCommande, @PathVariable("quantite") BigDecimal quantite);

    @PatchMapping(APP_ROOT + "/commandesclients/update/client/{idCommande}/{idClient}")
    ResponseEntity<ComandeClientDto> updateClient(@PathVariable("idCommande") Long idCommande, @PathVariable("idClient") Long idClient);

    @PatchMapping(APP_ROOT + "/commandesclients/update/article/{idCommande}/{idLigneCommande}/{idArticle}")
    ResponseEntity<ComandeClientDto> updateArticle(@PathVariable("idCommande") Long idCommande,
                                                    @PathVariable("idLigneCommande") Long idLigneCommande, @PathVariable("idArticle") Long idArticle);

    @DeleteMapping(APP_ROOT + "/commandesclients/delete/article/{idCommande}/{idLigneCommande}")
    ResponseEntity<ComandeClientDto> deleteArticle(@PathVariable("idCommande") Long idCommande, @PathVariable("idLigneCommande") Long idLigneCommande);

     @GetMapping(APP_ROOT + "/commandesclients/lignesCommande/{idCommande}")
    ResponseEntity<List<LigneCommandeClientDto>> findAllLignesCommandesClientByCommandeClientId(@PathVariable("idCommande") Long idCommande);

    @GetMapping(value = APP_ROOT + "/commandesclients/{idCommandeClient}/facture/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> genererFacturePdf(@PathVariable("idCommandeClient") Long idCommandeClient);


}
