package com.example.versuion.controller;

import com.example.versuion.Dto.ComandeClientDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.controller.api.CommandeClinetApi;
import com.example.versuion.models.EtatCommande;
import com.example.versuion.services.CommandeClinetService;
import com.example.versuion.services.FacturePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
@RestController
public class CommandeClinetControlleur implements CommandeClinetApi {

    private CommandeClinetService commandeClientService;
    private final FacturePdfService facturePdfService;

    @Autowired
    public CommandeClinetControlleur(CommandeClinetService commandeClientService, FacturePdfService facturePdfService) {
        this.commandeClientService = commandeClientService;
        this.facturePdfService = facturePdfService;
    }

    @Override
    public ResponseEntity<ComandeClientDto> save(ComandeClientDto dto) {
        return ResponseEntity.ok(commandeClientService.save(dto));
    }

    @Override
    public ResponseEntity<ComandeClientDto> findById(Long id) {
        return ResponseEntity.ok(commandeClientService.findById(id));
    }

    @Override
    public ResponseEntity<ComandeClientDto> findByCode(String code) {
        return ResponseEntity.ok(commandeClientService.findByCode(code));
    }

    @Override
    public ResponseEntity<List<ComandeClientDto>> findAll() {
        return ResponseEntity.ok(commandeClientService.findAll());
    }

    @Override
    public ResponseEntity<PageResponse<ComandeClientDto>> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        return ResponseEntity.ok(commandeClientService.findAllPaginated(page, size, sortBy, sortDir, search));
    }

    @Override
    public ResponseEntity delete(Long id) {
        commandeClientService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ComandeClientDto> updateEtatCommande(Long idCommande, EtatCommande etatCommande) {
        return ResponseEntity.ok(commandeClientService.updateEtatCommande(idCommande, etatCommande));
    }

    @Override
    public ResponseEntity<ComandeClientDto> updateQuantiteCommande(Long idCommande, Long idLigneCommande, BigDecimal quantite) {
        return ResponseEntity.ok(commandeClientService.updateQuantiteCommande(idCommande, idLigneCommande, quantite));
    }

    @Override
    public ResponseEntity<ComandeClientDto> updateClient(Long idCommande, Long idClient) {
        return ResponseEntity.ok(commandeClientService.updateClient(idCommande, idClient));
    }

    @Override
    public ResponseEntity<ComandeClientDto> updateArticle(Long idCommande, Long idLigneCommande, Long idArticle) {
        return ResponseEntity.ok(commandeClientService.updateArticle(idCommande, idLigneCommande, idArticle));
    }

    @Override
    public ResponseEntity<ComandeClientDto> deleteArticle(Long idCommande, Long idLigneCommande) {
        return ResponseEntity.ok(commandeClientService.deleteArticle(idCommande, idLigneCommande));
    }

    @Override
    public ResponseEntity<List<LigneCommandeClientDto>> findAllLignesCommandesClientByCommandeClientId(Long idCommande) {
        return ResponseEntity.ok(commandeClientService.findAllLignesCommandesClientByCommandeClientId(idCommande));
    }

    @Override
    public ResponseEntity<byte[]> genererFacturePdf(Long idCommandeClient) {
        byte[] pdf = facturePdfService.genererFacture(idCommandeClient);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture-" + idCommandeClient + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
