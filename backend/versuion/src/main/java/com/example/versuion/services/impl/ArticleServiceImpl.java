package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.LigneCommandeFournisseurDto;
import com.example.versuion.Dto.LigneVentDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.utiles.PaginationUtils;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.Category;
import com.example.versuion.models.LigneComandeClient;
import com.example.versuion.models.LigneComandeFournisseur;
import com.example.versuion.models.LigneVente;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.CategoryRepository;
import com.example.versuion.repository.LigneCommandeClientRepository;
import com.example.versuion.repository.LigneCommandeFournisseurRepository;
import com.example.versuion.repository.LigneVenteRepository;
import com.example.versuion.services.ArticleService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.validator.ArticleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    private final CategoryRepository categoryRepository;

    //Injection par constructeur
    public ArticleServiceImpl(ArticleRepository articleRepository,
                              LigneVenteRepository ligneVenteRepository,
                              LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository,
                              LigneCommandeClientRepository ligneCommandeClientRepository,
                              CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ArticleDto save(ArticleDto articleDto) {
        List<String> errors = ArticleValidator.validate(articleDto);
        if (!errors.isEmpty()) {
            log.error("Article is not valid {}", articleDto);
            throw new InvalidEntityException("l'article n'est pas valide", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }
        Long idCategory = articleDto.getCategory().getId();
        Category category = categoryRepository.findByIdTenant(idCategory)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune category avec l'ID " + idCategory + " n'a ete trouve dans la base",
                        ErrorCodes.CATEGORY_NOT_FOUND));

        Article article = ArticleDto.toEntity(articleDto);
        article.setCategory(category);
        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            article.setIdEntreprise(idEntreprise);
        }
        articleRepository.save(article);
        return ArticleDto.fromEntity(article);
    }

    @Override
    public ArticleDto findById(Long articleId) {
        if (articleId == null) {
            log.error("Article id is null");
            return null;
        }
        return articleRepository.findByIdTenant(articleId)
                .map(ArticleDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun article avec l'ID " + articleId + " n'a ete trouve dans la base",
                        ErrorCodes.ARTICLE_NOT_FOUND));
    }

    @Override
    public ArticleDto findByCodeArticle(String codeArticle) {
        if (codeArticle == null) {
            log.error("Article code is null");
            return null;
        }
        return articleRepository.findByCodeArticleTenant(codeArticle)
                .map(ArticleDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun article avec le code " + codeArticle + " n'a ete trouve dans la base",
                        ErrorCodes.ARTICLE_NOT_FOUND));
    }

    @Override
    public List<ArticleDto> findAll() {
        return articleRepository.findAllTenant().stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ArticleDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        Pageable pageable = PaginationUtils.pageable(page, size, sortBy, sortDir,
                List.of("id", "codeArticle", "designation", "prixUnitaireTTc", "seuilAlerte"));
        Page<Article> result = StringUtils.hasLength(search)
                ? articleRepository.findAllTenant(search, pageable)
                : articleRepository.findAllTenant(pageable);
        return PageResponse.from(result, ArticleDto::fromEntity);
    }

    @Override
    public List<ArticleDto> findArticlesSousSeuil() {
        return articleRepository.findArticlesSousSeuilTenant().stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long articleId) {
        if (articleId == null) {
            log.error("Article id is null");
            return;//por quitter la méthode
        }
        // Verifier que l'article appartient bien a l'entreprise courante avant suppression
        articleRepository.findByIdTenant(articleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun article avec l'ID " + articleId + " n'a ete trouve dans la base",
                        ErrorCodes.ARTICLE_NOT_FOUND));
        List<LigneComandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByArticleIdTenant(articleId);
        if (!ligneCommandeClients.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes client", ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        List<LigneComandeFournisseur> ligneCommandeFournisseurs = ligneCommandeFournisseurRepository.findAllByArticleIdTenant(articleId);
        if (!ligneCommandeFournisseurs.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des commandes fournisseur",
                    ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        List<LigneVente> ligneVentes = ligneVenteRepository.findAllByArticleIdTenant(articleId);
        if (!ligneVentes.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un article deja utilise dans des ventes",
                    ErrorCodes.ARTICLE_ALREADY_IN_USE);
        }
        articleRepository.deleteById(articleId);
    }

    @Override
    public List<LigneVentDto> findHistoriqueVentes(Long idArticle) {
        return ligneVenteRepository.findAllByArticleIdTenant(idArticle).stream()
                .map(LigneVentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeClientDto> findHistoriqueCommandeClient(Long idArticle) {
        return ligneCommandeClientRepository.findAllByArticleIdTenant(idArticle).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(Long idArticle) {
        return ligneCommandeFournisseurRepository.findAllByArticleIdTenant(idArticle).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleDto> findAllArticleByIdCategory(Long idCategory) {
        return articleRepository.findAllByCategoryIdTenant(idCategory).stream()
                .map(ArticleDto::fromEntity)
                .collect(Collectors.toList());
    }
}
