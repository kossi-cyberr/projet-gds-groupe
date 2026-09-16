package com.example.versuion.controller.api;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.LigneCommandeFournisseurDto;
import com.example.versuion.Dto.LigneVentDto;
import com.example.versuion.Dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.versuion.utiles.Constants.APP_ROOT;

@Tag(name = "Articles", description = "Gestion des articles")
public interface ArticleApi {

    @PostMapping(value = APP_ROOT + "/articles/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enregistrer un article", description = "Cette methode permet d'enregistrer ou modifier un article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "L'objet article cree / modifie"),
            @ApiResponse(responseCode = "400", description = "L'objet article n'est pas valide")
    })
    ArticleDto save(@RequestBody ArticleDto articleDto);
    //RequestBody : faire la transformation de l'objet de format json vers "ArticleDto"

    @GetMapping(value = APP_ROOT + "/articles/find/{idArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rechercher un article par ID", description = "Cette methode permet de chercher un article par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "L'article a ete trouve dans la BDD"),
            @ApiResponse(responseCode = "404", description = "Aucun article n'existe dans la BDD avec l'ID fourni")
    })
    ArticleDto findById(@PathVariable("idArticle") Long articleId);

    @GetMapping(value = APP_ROOT + "/articles/{codeArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rechercher un article par CODE", description = "Cette methode permet de chercher un article par son CODE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "L'article a ete trouve dans la BDD"),
            @ApiResponse(responseCode = "404", description = "Aucun article n'existe dans la BDD avec le CODE fourni")
    })
    ArticleDto findByCodeArticle(@PathVariable("codeArticle") String codeArticle);

    @GetMapping(value = APP_ROOT + "/articles/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Renvoi la liste des articles", description = "Cette methode permet de chercher et renvoyer la liste des articles qui existent dans la BDD")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "La liste des articles / Une liste vide")
    })
    List<ArticleDto> findAll();

    @GetMapping(value = APP_ROOT + "/articles/paged", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liste paginee et filtrable des articles", description = "Pagination + tri + recherche par designation ou code")
    PageResponse<ArticleDto> findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "id") String sortBy,
                                              @RequestParam(defaultValue = "asc") String sortDir,
                                              @RequestParam(required = false) String search);

    @GetMapping(value = APP_ROOT + "/articles/sous-seuil", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Articles sous le seuil d'alerte", description = "Renvoie les articles dont le stock reel est inferieur au seuilAlerte")
    List<ArticleDto> findArticlesSousSeuil();

    @GetMapping(value = APP_ROOT + "/articles/historique/vente/{idArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Historique des ventes d'un article")
    List<LigneVentDto> findHistoriqueVentes(@PathVariable("idArticle") Long idArticle);

    @GetMapping(value = APP_ROOT + "/articles/historique/commandeclient/{idArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Historique des commandes client d'un article")
    List<LigneCommandeClientDto> findHistoriaueCommandeClient(@PathVariable("idArticle") Long idArticle);

    @GetMapping(value = APP_ROOT + "/articles/historique/commandefournisseur/{idArticle}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Historique des commandes fournisseur d'un article")
    List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(@PathVariable("idArticle") Long idArticle);

    @GetMapping(value = APP_ROOT + "/articles/filter/category/{idCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liste des articles d'une categorie")
    List<ArticleDto> findAllArticleByIdCategory(@PathVariable("idCategory") Long idCategory);

    @DeleteMapping(value = APP_ROOT + "/articles/delete/{idArticle}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @Operation(summary = "Supprimer un article", description = "Cette methode permet de supprimer un article par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "L'article a ete supprime")
    })
    void delete(@PathVariable("idArticle") Long articleId);
}
