package com.example.versuion.services.impl;

import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.models.TypeMvtStk;
import com.example.versuion.repository.MvtStkRepository;
import com.example.versuion.services.ArticleService;
import com.example.versuion.services.MvtStkService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.validator.MvtStkValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MvtStkServiceImpl implements MvtStkService {

    private final MvtStkRepository repository;
    private final ArticleService articleService;

    public MvtStkServiceImpl(MvtStkRepository repository, ArticleService articleService) {
        this.repository = repository;
        this.articleService = articleService;
    }

    @Override
    public BigDecimal stockReelArticle(Long idArticle) {
        if (idArticle == null) {
            log.warn("ID article is NULL");
            return BigDecimal.valueOf(-1);
        }
        articleService.findById(idArticle);
        return repository.stockReelArticleTenant(idArticle);
    }

    @Override
    public List<MvtStkDto> mvtStkArticle(Long idArticle) {
        return repository.findAllByArticleIdTenant(idArticle).stream()
                .map(MvtStkDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<MvtStkDto> findAll() {
        return repository.findAllTenantOrderByIdDesc().stream()
                .map(MvtStkDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MvtStkDto entreeStock(MvtStkDto dto) {
        return entreePositive(dto, TypeMvtStk.ENTREE);
    }

    @Override
    public MvtStkDto sortieStock(MvtStkDto dto) {
        return sortieNegative(dto, TypeMvtStk.SORTIE);
    }

    @Override
    public MvtStkDto correctionStockPos(MvtStkDto dto) {
        return entreePositive(dto, TypeMvtStk.CORRECTION_POS);
    }

    @Override
    public MvtStkDto correctionStockNeg(MvtStkDto dto) {
        return sortieNegative(dto, TypeMvtStk.CORRECTION_NEG);
    }

    private MvtStkDto entreePositive(MvtStkDto dto, TypeMvtStk typeMvtStk) {
        // L'endpoint détermine le type : on l'applique avant validation pour une API cohérente
        if (dto != null) {
            dto.setTypeMvt(typeMvtStk);
            if (dto.getDateMvt() == null) {
                dto.setDateMvt(java.time.Instant.now());
            }
        }
        List<String> errors = MvtStkValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("MvtStk is not valid {}", dto);
            throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STK_NOT_VALID, errors);
        }
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue())
                )
        );
        dto.setTypeMvt(typeMvtStk);
        applyIdEntreprise(dto);
        return MvtStkDto.fromEntity(
                repository.save(MvtStkDto.toEntity(dto))
        );
    }

    private MvtStkDto sortieNegative(MvtStkDto dto, TypeMvtStk typeMvtStk) {
        // L'endpoint détermine le type : on l'applique avant validation pour une API cohérente
        if (dto != null) {
            dto.setTypeMvt(typeMvtStk);
            if (dto.getDateMvt() == null) {
                dto.setDateMvt(java.time.Instant.now());
            }
        }
        List<String> errors = MvtStkValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("MvtStk is not valid {}", dto);
            throw new InvalidEntityException("Le mouvement du stock n'est pas valide", ErrorCodes.MVT_STK_NOT_VALID, errors);
        }
        dto.setQuantite(
                BigDecimal.valueOf(
                        Math.abs(dto.getQuantite().doubleValue()) * -1
                )
        );
        dto.setTypeMvt(typeMvtStk);
        applyIdEntreprise(dto);
        return MvtStkDto.fromEntity(
                repository.save(MvtStkDto.toEntity(dto))
        );
    }

    private void applyIdEntreprise(MvtStkDto dto) {
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setIdEntreprise(idEntreprise);
        }
    }
}
