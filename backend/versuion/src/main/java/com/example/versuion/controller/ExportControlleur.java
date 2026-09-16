package com.example.versuion.controller;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.ComandeClientDto;
import com.example.versuion.Dto.FournisseurDto;
import com.example.versuion.Dto.VentesDto;
import com.example.versuion.services.ArticleService;
import com.example.versuion.services.ClientService;
import com.example.versuion.services.CommandeClinetService;
import com.example.versuion.services.ExportService;
import com.example.versuion.services.FournisseurService;
import com.example.versuion.services.VentesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.versuion.utiles.Constants.APP_ROOT;

/**
 * Endpoints d'export (Excel / CSV) des listes principales de l'entreprise courante.
 */
@RestController
public class ExportControlleur {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ArticleService articleService;
    private final ClientService clientService;
    private final FournisseurService fournisseurService;
    private final CommandeClinetService commandeClinetService;
    private final VentesService ventesService;
    private final ExportService exportService;

    public ExportControlleur(ArticleService articleService, ClientService clientService,
                             FournisseurService fournisseurService, CommandeClinetService commandeClinetService,
                             VentesService ventesService, ExportService exportService) {
        this.articleService = articleService;
        this.clientService = clientService;
        this.fournisseurService = fournisseurService;
        this.commandeClinetService = commandeClinetService;
        this.ventesService = ventesService;
        this.exportService = exportService;
    }

    // ------------------------------------------------------------------
    // Articles
    // ------------------------------------------------------------------

    @GetMapping(APP_ROOT + "/exports/articles/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportArticlesExcel() {
        List<List<Object>> rows = articleService.findAll().stream()
                .map(a -> row(a.getCodeArticle(), a.getDesignation(), a.getPrixUnitaire(),
                        a.getTauxTva(), a.getPrixUnitaireTTc(), a.getSeuilAlerte(),
                        a.getCategory() != null ? a.getCategory().getDesignation() : ""))
                .collect(Collectors.toList());
        return excelResponse("Articles", new String[]{"Code", "Désignation", "PU HT", "TVA %", "PU TTC", "Seuil", "Catégorie"}, rows, "articles");
    }

    @GetMapping(APP_ROOT + "/exports/articles/csv")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportArticlesCsv() {
        List<List<Object>> rows = articleService.findAll().stream()
                .map(a -> row(a.getCodeArticle(), a.getDesignation(), a.getPrixUnitaire(),
                        a.getPrixUnitaireTTc(),
                        a.getCategory() != null ? a.getCategory().getDesignation() : ""))
                .collect(Collectors.toList());
        return csvResponse(new String[]{"Code", "Désignation", "PU HT", "PU TTC", "Catégorie"}, rows, "articles");
    }

    // ------------------------------------------------------------------
    // Clients
    // ------------------------------------------------------------------

    @GetMapping(APP_ROOT + "/exports/clients/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportClientsExcel() {
        List<List<Object>> rows = clientService.findAll().stream()
                .map(c -> row(c.getNom(), c.getPrenom(), c.getMail(), c.getNumTel(),
                        c.getAdresse() != null ? c.getAdresse().getVille() : ""))
                .collect(Collectors.toList());
        return excelResponse("Clients", new String[]{"Nom", "Prénom", "Email", "Téléphone", "Ville"}, rows, "clients");
    }

    @GetMapping(APP_ROOT + "/exports/clients/csv")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportClientsCsv() {
        List<List<Object>> rows = clientService.findAll().stream()
                .map(c -> row(c.getNom(), c.getPrenom(), c.getMail(), c.getNumTel()))
                .collect(Collectors.toList());
        return csvResponse(new String[]{"Nom", "Prénom", "Email", "Téléphone"}, rows, "clients");
    }

    // ------------------------------------------------------------------
    // Fournisseurs
    // ------------------------------------------------------------------

    @GetMapping(APP_ROOT + "/exports/fournisseurs/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportFournisseursExcel() {
        List<List<Object>> rows = fournisseurService.findAll().stream()
                .map(f -> row(f.getNom(), f.getPrenom(), f.getMail(), f.getNumTel()))
                .collect(Collectors.toList());
        return excelResponse("Fournisseurs", new String[]{"Nom", "Prénom", "Email", "Téléphone"}, rows, "fournisseurs");
    }

    @GetMapping(APP_ROOT + "/exports/fournisseurs/csv")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportFournisseursCsv() {
        List<List<Object>> rows = fournisseurService.findAll().stream()
                .map(f -> row(f.getNom(), f.getPrenom(), f.getMail(), f.getNumTel()))
                .collect(Collectors.toList());
        return csvResponse(new String[]{"Nom", "Prénom", "Email", "Téléphone"}, rows, "fournisseurs");
    }

    // ------------------------------------------------------------------
    // Commandes clients
    // ------------------------------------------------------------------

    @GetMapping(APP_ROOT + "/exports/commandesclients/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportCommandesClientsExcel() {
        List<List<Object>> rows = commandeClinetService.findAll().stream()
                .map(c -> row(c.getCode(),
                        c.getDateComande() != null ? c.getDateComande().atZone(java.time.ZoneId.systemDefault()).format(DATE_FMT) : "",
                        c.getEtatCommande() != null ? c.getEtatCommande().name() : "",
                        c.getClient() != null ? c.getClient().getNom() : ""))
                .collect(Collectors.toList());
        return excelResponse("Commandes", new String[]{"Code", "Date", "État", "Client"}, rows, "commandes-clients");
    }

    // ------------------------------------------------------------------
    // Ventes
    // ------------------------------------------------------------------

    @GetMapping(APP_ROOT + "/exports/commandesclients/csv")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportCommandesClientsCsv() {
        List<List<Object>> rows = commandeClinetService.findAll().stream()
                .map(c -> row(c.getCode(),
                        c.getDateComande() != null ? c.getDateComande().atZone(java.time.ZoneId.systemDefault()).format(DATE_FMT) : "",
                        c.getEtatCommande() != null ? c.getEtatCommande().name() : "",
                        c.getClient() != null ? c.getClient().getNom() : ""))
                .collect(Collectors.toList());
        return csvResponse(new String[]{"Code", "Date", "État", "Client"}, rows, "commandes-clients");
    }

    @GetMapping(APP_ROOT + "/exports/ventes/excel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportVentesExcel() {
        List<List<Object>> rows = ventesService.findAll().stream()
                .map(v -> row(v.getCode(),
                        v.getDateVente() != null ? v.getDateVente().atZone(java.time.ZoneId.systemDefault()).format(DATE_FMT) : "",
                        v.getCommentaire() != null ? v.getCommentaire() : ""))
                .collect(Collectors.toList());
        return excelResponse("Ventes", new String[]{"Code", "Date", "Commentaire"}, rows, "ventes");
    }

    @GetMapping(APP_ROOT + "/exports/ventes/csv")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportVentesCsv() {
        List<List<Object>> rows = ventesService.findAll().stream()
                .map(v -> row(v.getCode(),
                        v.getDateVente() != null ? v.getDateVente().atZone(java.time.ZoneId.systemDefault()).format(DATE_FMT) : "",
                        v.getCommentaire() != null ? v.getCommentaire() : ""))
                .collect(Collectors.toList());
        return csvResponse(new String[]{"Code", "Date", "Commentaire"}, rows, "ventes");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Construit une ligne de tableau en acceptant les valeurs null. */
    private static List<Object> row(Object... values) {
        return Arrays.asList(values);
    }

    private ResponseEntity<byte[]> excelResponse(String sheetName, String[] headers,
                                                 List<List<Object>> rows, String filename) {
        byte[] data = exportService.exporterExcel(sheetName, headers, rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    private ResponseEntity<byte[]> csvResponse(String[] headers, List<List<Object>> rows, String filename) {
        byte[] data = exportService.exporterCsv(headers, rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
