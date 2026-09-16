package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.ComandeClientDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.Client;
import com.example.versuion.models.CommandeClient;
import com.example.versuion.models.EtatCommande;
import com.example.versuion.models.LigneComandeClient;
import com.example.versuion.models.SourceMvtStk;
import com.example.versuion.models.TypeMvtStk;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.ClientRepository;
import com.example.versuion.repository.CommandeClientRepository;
import com.example.versuion.repository.LigneCommandeClientRepository;
import com.example.versuion.services.CommandeClinetService;
import com.example.versuion.services.MvtStkService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.utiles.PaginationUtils;
import com.example.versuion.validator.ArticleValidator;
import com.example.versuion.validator.CommandeClientValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class CommandeClinetServiceImpl implements CommandeClinetService {

    private final CommandeClientRepository commandeClientRepository;
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final MvtStkService mvtStkService;

    public CommandeClinetServiceImpl(CommandeClientRepository commandeClientRepository,
                                     ClientRepository clientRepository,
                                     ArticleRepository articleRepository,
                                     LigneCommandeClientRepository ligneCommandeClientRepository,
                                     MvtStkService mvtStkService) {
        this.commandeClientRepository = commandeClientRepository;
        this.clientRepository = clientRepository;
        this.articleRepository = articleRepository;
        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
        this.mvtStkService = mvtStkService;
    }

    @Override
    @Transactional
    public ComandeClientDto save(ComandeClientDto dto) {
        List<String> errors = CommandeClientValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Commande client n'est pas valide");
            throw new InvalidEntityException("La commande client n'est pas valide", ErrorCodes.COMMANDE_CLIENT_NOT_VALID, errors);
        }

        if (dto.getId() != null && dto.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        //Verifier l'existence du client dans la base
        Client client = clientRepository.findByIdTenant(dto.getClient().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun client avec l'ID " + dto.getClient().getId() + " n'a ete trouve dans la BDD",
                        ErrorCodes.CLIENT_NOT_FOUND));

        List<String> articleErrors = new ArrayList<>();

        //Verification sur les articles
        if (dto.getLigneComandeClientList() != null) {
            dto.getLigneComandeClientList().forEach(ligCmdClt -> {
                if (ligCmdClt.getArticle() != null && ligCmdClt.getArticle().getId() != null) {
                    boolean exists = articleRepository.findByIdTenant(ligCmdClt.getArticle().getId()).isPresent();
                    if (!exists) {
                        articleErrors.add("L'article avec l'ID " + ligCmdClt.getArticle().getId() + " n'existe pas");
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
            if (dto.getLigneComandeClientList() != null) {
                dto.getLigneComandeClientList().forEach(lig -> lig.setIdEntreprise(idEntreprise));
            }
        }

        CommandeClient savedCmdClt = commandeClientRepository.save(ComandeClientDto.toEntity(dto));
        if (dto.getLigneComandeClientList() != null) {
            dto.getLigneComandeClientList().forEach(ligCmdClt -> {
                LigneComandeClient ligneCommandeClient = LigneCommandeClientDto.toEntity(ligCmdClt);
                ligneCommandeClient.setCommandeClient(savedCmdClt);
                ligneCommandeClientRepository.save(ligneCommandeClient);
            });
        }
        return ComandeClientDto.fromEntity(savedCmdClt);
    }

    @Override
    public ComandeClientDto findById(Long id) {
        if (id == null) {
            log.error("Commande client ID is NULL");
            return null;
        }
        return commandeClientRepository.findByIdTenant(id)
                .map(ComandeClientDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande client n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                ));
    }

    @Override
    public ComandeClientDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Commande client CODE is NULL");
            return null;
        }
        return commandeClientRepository.findByCodeTenant(code)
                .map(ComandeClientDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande client n'a ete trouve avec le CODE " + code, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND
                ));
    }

    @Override
    public List<ComandeClientDto> findAll() {
        return commandeClientRepository.findAllTenant().stream()
                .map(ComandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ComandeClientDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        Pageable pageable = PaginationUtils.pageable(page, size, sortBy, sortDir,
                List.of("id", "code", "dateComande", "etatCommande"));
        Page<CommandeClient> result = StringUtils.hasLength(search)
                ? commandeClientRepository.findAllTenant(search, pageable)
                : commandeClientRepository.findAllTenant(pageable);
        return PageResponse.from(result, ComandeClientDto::fromEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Commande client ID is NULL");
            return;
        }
        // Verifier que la commande appartient bien a l'entreprise courante avant suppression
        commandeClientRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune commande client n'a ete trouve avec l'ID " + id, ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
        List<LigneComandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientIdTenant(id);
        if (!ligneCommandeClients.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer une commande client deja utilisee",
                    ErrorCodes.COMMANDE_CLIENT_ALREADY_IN_USE);
        }
        commandeClientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ComandeClientDto updateEtatCommande(Long idCommande, EtatCommande etatCommande) {
        checkIdCommande(idCommande);

        if (etatCommande == null) {
            log.error("L'etat de la commande client is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un etat null", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        ComandeClientDto comandeClientDto = checkEtatCommande(idCommande);
        comandeClientDto.setEtatCommande(etatCommande);

        CommandeClient commandeClientSaved = commandeClientRepository.save(ComandeClientDto.toEntity(comandeClientDto));
        if (comandeClientDto.isCommandeLivree()) {
            updateMvtStk(idCommande);
        }
        return ComandeClientDto.fromEntity(commandeClientSaved);
    }

    @Override
    @Transactional
    public ComandeClientDto updateQuantiteCommande(Long idCommande, Long idLigneCommande, BigDecimal quantite) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
            log.error("la quantité de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une quantite null ou ZERO",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }

        ComandeClientDto comandeClientDto = checkEtatCommande(idCommande);
        LigneComandeClient ligneComandeClient = ligneCommandeClientRepository.findByIdTenant(idLigneCommande)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune ligne de commande client avec l'id " + idLigneCommande + " a été trouver dans la BDD",
                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
        ligneComandeClient.setQuantite(quantite);
        ligneCommandeClientRepository.save(ligneComandeClient);
        return comandeClientDto;
    }

    @Override
    @Transactional
    public ComandeClientDto updateClient(Long idCommande, Long idClient) {
        checkIdCommande(idCommande);

        if (idClient == null) {
            log.error("client ID is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID du client null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        ComandeClientDto comandeClientDto = checkEtatCommande(idCommande);

        Client client = clientRepository.findByIdTenant(idClient)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun client n'a ete trouve avec l'ID " + idClient, ErrorCodes.CLIENT_NOT_FOUND));
        comandeClientDto.setClient(ClientDto.fromEntity(client));

        return ComandeClientDto.fromEntity(
                commandeClientRepository.save(ComandeClientDto.toEntity(comandeClientDto))
        );
    }

    @Override
    @Transactional
    public ComandeClientDto updateArticle(Long idCommande, Long idLigneCommande, Long newIdArticle) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);
        checkIdArticle(newIdArticle, "noveau");
        ComandeClientDto comandeClientDto = checkEtatCommande(idCommande);

        LigneComandeClient ligneCommandeClientToSaved = findLigneCommandeClient(idLigneCommande);

        Article article = articleRepository.findByIdTenant(newIdArticle)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun article n'a ete trouve avec l'ID " + newIdArticle, ErrorCodes.ARTICLE_NOT_FOUND));

        List<String> errors = ArticleValidator.validate(ArticleDto.fromEntity(article));
        if (!errors.isEmpty()) {
            throw new InvalidEntityException("Article invalid", ErrorCodes.ARTICLE_NOT_VALID, errors);
        }

        ligneCommandeClientToSaved.setArticle(article);
        ligneCommandeClientRepository.save(ligneCommandeClientToSaved);

        return comandeClientDto;
    }

    @Override
    @Transactional
    public ComandeClientDto deleteArticle(Long idCommande, Long idLigneCommande) {
        checkIdCommande(idCommande);
        checkIdLigneCommande(idLigneCommande);

        ComandeClientDto commandeClient = checkEtatCommande(idCommande);

        findLigneCommandeClient(idLigneCommande);
        ligneCommandeClientRepository.deleteById(idLigneCommande);

        return commandeClient;
    }

    @Override
    public List<LigneCommandeClientDto> findAllLignesCommandesClientByCommandeClientId(Long idCommande) {
        return ligneCommandeClientRepository.findAllByCommandeClientIdTenant(idCommande).stream()
                .map(LigneCommandeClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    private void checkIdCommande(Long idCommande) {
        if (idCommande == null) {
            log.error("Commande client ID is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un ID null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }

    private void checkIdLigneCommande(Long idLigneCommande) {
        if (idLigneCommande == null) {
            log.error("L'ID de la ligne commande is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec une ligne de commande null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }

    private void checkIdArticle(Long idArticle, String msg) {
        if (idArticle == null) {
            log.error("L'ID de " + msg + " is NULL");
            throw new InvalidOperationException("Impossible de modifier l'etat de la commande avec un " + msg + " ID article null",
                    ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
    }

    private ComandeClientDto checkEtatCommande(Long idCommande) {
        ComandeClientDto commandeClient = findById(idCommande);
        if (commandeClient.isCommandeLivree()) {
            throw new InvalidOperationException("Impossible de modifier la commande lorsqu'elle est livree", ErrorCodes.COMMANDE_CLIENT_NON_MODIFIABLE);
        }
        return commandeClient;
    }

    private LigneComandeClient findLigneCommandeClient(Long idLigneCommande) {
        return ligneCommandeClientRepository.findByIdTenant(idLigneCommande)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune ligne commande client n'a ete trouve avec l'ID " + idLigneCommande,
                        ErrorCodes.COMMANDE_CLIENT_NOT_FOUND));
    }

    private void updateMvtStk(Long idCommande) {
        List<LigneComandeClient> ligneCommandeClients = ligneCommandeClientRepository.findAllByCommandeClientIdTenant(idCommande);
        ligneCommandeClients.forEach(this::effectuerSortie);
    }

    private void effectuerSortie(LigneComandeClient lig) {
        MvtStkDto mvtStkDto = MvtStkDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(TypeMvtStk.SORTIE)
                .sourceMvt(SourceMvtStk.COMMANDE_CLIENT)
                .quantite(lig.getQuantite())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStkService.sortieStock(mvtStkDto);
    }
}
