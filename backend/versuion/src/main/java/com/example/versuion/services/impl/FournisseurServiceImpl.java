package com.example.versuion.services.impl;

import com.example.versuion.Dto.FournisseurDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.ComandeFournisseur;
import com.example.versuion.repository.CommandeFournisseurRepository;
import com.example.versuion.repository.FournisseurRepository;
import com.example.versuion.services.FournisseurService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.utiles.PaginationUtils;
import com.example.versuion.validator.FournisseurValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;
    private final CommandeFournisseurRepository commandeFournisseurRepository;

    public FournisseurServiceImpl(FournisseurRepository fournisseurRepository,
                                  CommandeFournisseurRepository commandeFournisseurRepository) {
        this.fournisseurRepository = fournisseurRepository;
        this.commandeFournisseurRepository = commandeFournisseurRepository;
    }

    @Override
    public FournisseurDto save(FournisseurDto dto) {
        List<String> errors = FournisseurValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Fournisseur is not valid {0}", dto);
            throw new InvalidEntityException("Le fournisseur n'est pas valide", ErrorCodes.FOURNISSEUR_NOT_VALID, errors);
        }
        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setIdEntreprise(idEntreprise);
        }
        return FournisseurDto.fromEntity(fournisseurRepository.save(FournisseurDto.toEntity(dto)));
    }

    @Override
    public FournisseurDto update(Long id, FournisseurDto dto) {
        List<String> errors = FournisseurValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Fournisseur is not valid {0}", dto);
            throw new InvalidEntityException("Le fournisseur n'est pas valide", ErrorCodes.FOURNISSEUR_NOT_VALID, errors);
        }
        FournisseurDto existant = findById(id);
        dto.setId(id);
        // Conserver le rattachement entreprise existant
        dto.setIdEntreprise(existant.getIdEntreprise());
        return FournisseurDto.fromEntity(fournisseurRepository.save(FournisseurDto.toEntity(dto)));
    }

    @Override
    public FournisseurDto findById(Long id) {
        if (id == null) {
            log.error("Fournisseur ID is null");
            return null;
        }
        return fournisseurRepository.findByIdTenant(id)
                .map(FournisseurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun fournisseur avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND));
    }

    @Override
    public List<FournisseurDto> findAll() {
        return fournisseurRepository.findAllTenant().stream()
                .map(FournisseurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<FournisseurDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        Pageable pageable = PaginationUtils.pageable(page, size, sortBy, sortDir,
                List.of("id", "nom", "prenom", "mail", "numTel"));
        Page<com.example.versuion.models.Fournisseur> result = StringUtils.hasLength(search)
                ? fournisseurRepository.findAllTenant(search, pageable)
                : fournisseurRepository.findAllTenant(pageable);
        return PageResponse.from(result, FournisseurDto::fromEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Fournisseur ID is null");
            return;
        }
        // Verifier que le fournisseur appartient bien a l'entreprise courante avant suppression
        fournisseurRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun fournisseur avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.FOURNISSEUR_NOT_FOUND));
        List<ComandeFournisseur> commandeFournisseur = commandeFournisseurRepository.findAllByFournisseurIdTenant(id);
        if (!commandeFournisseur.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un fournisseur qui a deja des commandes",
                    ErrorCodes.FOURNISSEUR_ALREADY_IN_USE);
        }
        fournisseurRepository.deleteById(id);
    }
}
