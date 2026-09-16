package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.ComandeFournisseurDto;
import com.example.versuion.Dto.FournisseurDto;
import com.example.versuion.Dto.LigneCommandeFournisseurDto;
import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.ComandeFournisseur;
import com.example.versuion.models.EtatCommande;
import com.example.versuion.models.Fournisseur;
import com.example.versuion.models.LigneComandeFournisseur;
import com.example.versuion.models.SourceMvtStk;
import com.example.versuion.models.TypeMvtStk;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.CommandeFournisseurRepository;
import com.example.versuion.repository.FournisseurRepository;
import com.example.versuion.repository.LigneCommandeFournisseurRepository;
import com.example.versuion.services.CommandeFournisseurService;
import com.example.versuion.services.MvtStkService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.validator.ArticleValidator;
import com.example.versuion.validator.CommandeFournisseurValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommandeFournisseurServiceImpl implements CommandeFournisseurService {

    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ArticleRepository articleRepository;
    private final LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    private final MvtStkService mvtStkService;

    public CommandeFournisseurServiceImpl(CommandeFournisseurRepository commandeFournisseurRepository,
                                          FournisseurRepository fournisseurRepository,
                                          ArticleRepository articleRepository,
                                          LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository,
                                          MvtStkService mvtStkService) {
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.articleRepository = articleRepository;
        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
        this.mvtStkService = mvtStkService;
    }

    @Override
    @Transactional
    public ComandeFournisseurDto save(ComandeFournisseurDto dto) {
        List<String> errors = CommandeFournisseurValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Commande fournisseur n'est pas valide");
            throw new InvalidEntityException("La commande fournisseur n'est pas valide", ErrorCodes.COMMANDE_FOURNISSEUR_NOT_VALID, errors);
        }

        //Verifier l'existence du fournisseur dans la base
        fournisseurRepository.findByIdTenant(dto.getFournisseur().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun fournisseur avec l'ID " + dto.getFournisseur().getId() + " n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND));

        List<String> articleErrors = new ArrayList<>();

        //Verification sur les articles
        if (dto.getLigneComandeFournisseurList() != null) {
            dto.getLigneComandeFournisseurList().forEach(ligCmdFrs -> {
                if (ligCmdFrs.getArticle() != null && ligCmdFrs.getArticle().getId() != null) {
                    boolean exists = articleRepository.findByIdTenant(ligCmdFrs.getArticle().getId()).isPresent();
                    if (!exists) {
                        articleErrors.add("L'article avec l'ID " + ligCmdFrs.getArticle().getId() + " n'existe pas");
                    }
                } else {
                    articleErrors.add("Impossible d'enregister une commande avec un article NULL");
                }
            });
        }

        if (!articleErrors.isEmpty()) {
            log.warn("Article(s) introuvable(s)");
            throw new InvalidEntityException("Article n'existe pas dans la BDD", ErrorCodes.ARTICLE_NOT_FOUND, articleErrors);
        }

        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setIdEntreprise(idEntreprise);
            if (dto.getLigneComandeFournisseurList() != null) {
                dto.getLigneComandeFournisseurList().forEach(lig -> lig.setIdEntreprise(idEntreprise));
            }
        }

        ComandeFournisseur savedCmdFrs = commandeFournisseurRepository.save(ComandeFournisseurDto.toEntity(dto));
        if (dto.getLigneComandeFournisseurList() != null) {
            dto.getLigneComandeFournisseurList().forEach(ligCmdFrs -> {
                LigneComandeFournisseur ligneComandeFournisseur = LigneCommandeFournisseurDto.toEntity(ligCmdFrs);
                ligneComandeFournisseur.setCommandeFournisseur(savedCmdFrs);
                ligneCommandeFournisseurRepository.save(ligneComandeFournisseur);
            });
        }
        return ComandeFournisseurDto.fromEntity(savedCmdFrs);
    }

    @Override
    public ComandeFournisseurDto findById(Long id) {
        if (id == null) {
            log.error("Commande fournisseur ID is NULL");
            return null;
        }
        return commandeFournisseurRepository.findByIdTenant(id)
                .map(ComandeFournisseurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
                ));
    }

    @Override
    public ComandeFournisseurDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Commande fournisseur CODE is NULL");
            return null;
        }
        return commandeFournisseurRepository.findByCodeTenant(code)
                .map(ComandeFournisseurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur n'a ete trouve avec le CODE " + code, ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND
                ));
    }

    @Override
    public List<ComandeFournisseurDto> findAll() {
        return commandeFournisseurRepository.findAllTenant().stream()
                .map(ComandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Commande fournisseur ID is NULL");
            return;
        }
        // Verifier que la commande appartient bien a l'entreprise courante avant suppression
        commandeFournisseurRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande fournisseur n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND));
        List<LigneComandeFournisseur> ligneComandeFournisseurs = ligneCommandeFournisseurRepository.findAllByCommandeFournisseurIdTenant(id);
        if (!ligneComandeFournisseurs.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer une commande Fournisseur deja utilisee",
                    ErrorCodes.COMMANDE_FOURNISSEUR_ALREADY_IN_USE);
        }
        commandeFournisseurRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ComandeFournisseurDto updateEtatCommande(Long idCommande, EtatCommande etatCommande) {
        checkIdCommande(idCommande);
        if (etatCommande == null) {
            log.error("L'etat de la commande fournisseur is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        ComandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        commandeFournisseur.setEtatCommande(etatCommande);

        ComandeFournisseur savedCommande = commandeFournisseurRepository.save(ComandeFournisseurDto.toEntity(commandeFournisseur));
        if (commandeFournisseur.isCommandeLivree()) {
            updateMvtStk(idCommande);
        }
        return ComandeFournisseurDto.fromEntity(savedCommande);
    }

    @Override
    @Transactional
    public ComandeFournisseurDto updateQuantiteCommande(Long idCommande, Long idLigneCommande, BigDecimal quantite) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }

        ComandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        LigneComandeFournisseur ligneCommandeFounisseur = findLigneCommandeFournisseur(idLigneCommande);
        ligneCommandeFounisseur.setQuantite(quantite);
        ligneCommandeFournisseurRepository.save(ligneCommandeFounisseur);

        return commandeFournisseur;
    }

    @Override
    @Transactional
    public ComandeFournisseurDto updateFournisseur(Long idCommande, Long idFournisseur) {
        checkIdCommande(idCommande);
        if (idFournisseur == null) {
            log.error("L'ID du fournisseur is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID fournisseur null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        ComandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        Fournisseur fournisseur = fournisseurRepository.findByIdTenant(idFournisseur)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun fournisseur n'a ete trouve avec l'ID " + idFournisseur, ErrorCodes.FOURNISSEUR_NOT_FOUND));
        commandeFournisseur.setFournisseur(FournisseurDto.fromEntity(fournisseur));

        return ComandeFournisseurDto.fromEntity(
                commandeFournisseurRepository.save(ComandeFournisseurDto.toEntity(commandeFournisseur))
        );
    }

    @Override
    @Transactional
    public ComandeFournisseurDto updateArticle(Long idCommande, Long idLigneCommande, Long idArticle) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(idArticle, "nouvel");

        ComandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);

        LigneComandeFournisseur ligneCommandeFournisseurToSaved = findLigneCommandeFournisseur(idLigneCommande);

        Article article = articleRepository.findByIdTenant(idArticle)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun article n'a ete trouve avec l'ID " + idArticle, ErrorCodes.ARTICLE_NOT_FOUND));

        List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(article));
        if (!errors.isEmpty()) {
            throw new InvalidEntityException("Article invalid", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }

        ligneCommandeFournisseurToSaved.setArticle(article);
        ligneCommandeFournisseurRepository.save(ligneCommandeFournisseurToSaved);

        return commandeFournisseur;
    }

    @Override
    @Transactional
    public ComandeFournisseurDto deleteArticle(Long idCommande, Long idLigneCommande) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        ComandeFournisseurDto commandeFournisseur = checkEtatCommande(idCommande);
        findLigneCommandeFournisseur(idLigneCommande);
        ligneCommandeFournisseurRepository.deleteById(idLigneCommande);

        return commandeFournisseur;
    }

    @Override
    public List<LigneCommandeFournisseurDto> findAllLignesCommandesFournisseurByCommandeFournisseurId(Long idCommande) {
        return ligneCommandeFournisseurRepository.findAllByCommandeFournisseurIdTenant(idCommande).stream()
                .map(LigneCommandeFournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    private ComandeFournisseurDto checkEtatCommande(Long idCommande) {
        ComandeFournisseurDto commandeFournisseur = findById(idCommande);
        if (commandeFournisseur.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
        return commandeFournisseur;
    }

    private LigneComandeFournisseur findLigneCommandeFournisseur(Long idLigneCommande) {
        return ligneCommandeFournisseurRepository.findByIdTenant(idLigneCommande)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune ligne commande fournisseur n'a ete trouve avec l'ID " + idLigneCommande,
                        ErrorCodes.COMMANDE_FOURNISSEUR_NOT_FOUND));
    }

    private void checkIdCommande(Long idCommande) {
        if (idCommande == null) {
            log.error("Commande fournisseur ID is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void checkIdLigneCommande(Long idLigneCommande) {
        if (idLigneCommande == null) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une ligne de commande null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void checkIdArticle(Long idArticle, String msg) {
        if (idArticle == null) {
            log.error("L'ID de " + msg + " is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
                    ErrorCodes.COMMANDE_FOURNISSEUR_NON_MODIFIABLE);
        }
    }

    private void updateMvtStk(Long idCommande) {
        List<LigneComandeFournisseur> ligneCommandeFournisseur = ligneCommandeFournisseurRepository.findAllByCommandeFournisseurIdTenant(idCommande);
        ligneCommandeFournisseur.forEach(this::effectuerEntree);
    }

    private void effectuerEntree(LigneComandeFournisseur lig) {
        MvtStkDto mvtStkDto = MvtStkDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(TypeMvtStk.ENTREE)
                .sourceMvt(SourceMvtStk.COMMANDE_FOURNISSEUR)
                .quantite(lig.getQuantite())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStkService.entreeStock(mvtStkDto);
    }
}
