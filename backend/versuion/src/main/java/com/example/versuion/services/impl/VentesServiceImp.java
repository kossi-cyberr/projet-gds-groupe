package com.example.versuion.services.impl;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.LigneVentDto;
import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.Dto.VentesDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Article;
import com.example.versuion.models.LigneVente;
import com.example.versuion.models.SourceMvtStk;
import com.example.versuion.models.TypeMvtStk;
import com.example.versuion.models.Ventes;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.LigneVenteRepository;
import com.example.versuion.repository.VentesRepository;
import com.example.versuion.services.MvtStkService;
import com.example.versuion.services.VentesService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.utiles.PaginationUtils;
import com.example.versuion.validator.VentesValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VentesServiceImp implements VentesService {

    private final ArticleRepository articleRepository;
    private final VentesRepository ventesRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final MvtStkService mvtStkService;

    public VentesServiceImp(ArticleRepository articleRepository, VentesRepository ventesRepository,
                            LigneVenteRepository ligneVenteRepository, MvtStkService mvtStkService) {
        this.articleRepository = articleRepository;
        this.ventesRepository = ventesRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.mvtStkService = mvtStkService;
    }

    @Override
    @Transactional
    public VentesDto save(VentesDto dto) {
        List<String> errors = VentesValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Ventes n'est pas valide");
            throw new InvalidEntityException("L'objet vente n'est pas valide", ErrorCodes.VENTE_NOT_VALID, errors);
        }
        List<String> articleErrors = new ArrayList<>();

        //verifier pour chaque ligne de vente si on a un article
        dto.getLigneVentes().forEach(ligneVenteDto -> {
            if (ligneVenteDto.getArticle() != null && ligneVenteDto.getArticle().getId() != null) {
                boolean exists = articleRepository.findByIdTenant(ligneVenteDto.getArticle().getId()).isPresent();
                if (!exists) {
                    articleErrors.add("Aucun article avec l'ID " + ligneVenteDto.getArticle().getId() + " n'a ete trouve dans la BDD");
                }
            } else {
                articleErrors.add("Impossible d'enregistrer une vente avec un article NULL");
            }
        });

        if (!articleErrors.isEmpty()) {
            log.error("One or more articles were not found in the DB");
            throw new InvalidEntityException("Un ou plusieurs articles n'ont pas ete trouve dans la BDD", ErrorCodes.VENTE_NOT_VALID, articleErrors);
        }

        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setIdEntreprise(idEntreprise);
            dto.getLigneVentes().forEach(lig -> lig.setIdEntreprise(idEntreprise));
        }

        Ventes savedVentes = ventesRepository.save(VentesDto.toEntity(dto));

        dto.getLigneVentes().forEach(ligneVenteDto -> {
            LigneVente ligneVente = LigneVentDto.toEntity(ligneVenteDto);
            ligneVente.setVente(savedVentes);
            ligneVenteRepository.save(ligneVente);
            updateMvtStk(ligneVente);
        });

        return VentesDto.fromEntity(savedVentes);
    }

    @Override
    public VentesDto findById(Long id) {
        if (id == null) {
            log.error("Ventes ID is NULL");
            return null;
        }
        return ventesRepository.findByIdTenant(id)
                .map(VentesDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Aucun vente n'a ete trouve dans la BDD", ErrorCodes.VENTE_NOT_FOUND));
    }

    @Override
    public VentesDto findByCode(String code) {
        if (!StringUtils.hasLength(code)) {
            log.error("Vente CODE is NULL");
            return null;
        }
        return ventesRepository.findByCodeTenant(code)
                .map(VentesDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune vente client n'a ete trouve avec le CODE " + code, ErrorCodes.VENTE_NOT_FOUND));
    }

    @Override
    public List<VentesDto> findAll() {
        return ventesRepository.findAllTenant().stream()
                .map(VentesDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<VentesDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        Pageable pageable = PaginationUtils.pageable(page, size, sortBy, sortDir,
                List.of("id", "code", "dateVente", "commentaire"));
        Page<Ventes> result = StringUtils.hasLength(search)
                ? ventesRepository.findAllTenant(search, pageable)
                : ventesRepository.findAllTenant(pageable);
        return PageResponse.from(result, VentesDto::fromEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Vente ID is NULL");
            return;
        }
        // Verifier que la vente appartient bien a l'entreprise courante avant suppression
        ventesRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune vente n'a ete trouve dans la BDD", ErrorCodes.VENTE_NOT_FOUND));
        List<LigneVente> ligneVentes = ligneVenteRepository.findAllByVenteIdTenant(id);
        if (!ligneVentes.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer cette vente", ErrorCodes.VENTE_ALREADY_IN_USE);
        }
        ventesRepository.deleteById(id);
    }

    private void updateMvtStk(LigneVente lig) {
        MvtStkDto mvtStkDto = MvtStkDto.builder()
                .article(ArticleDto.fromEntity(lig.getArticle()))
                .dateMvt(Instant.now())
                .typeMvt(TypeMvtStk.SORTIE)
                .sourceMvt(SourceMvtStk.VENTE)
                .quantite(lig.getQuantie())
                .idEntreprise(lig.getIdEntreprise())
                .build();
        mvtStkService.sortieStock(mvtStkDto);
    }
}
