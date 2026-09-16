package com.example.versuion.services.impl;

import com.example.versuion.Dto.EntrepriseDto;
import com.example.versuion.Dto.RoleDto;
import com.example.versuion.Dto.UtilisateurDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.repository.EntrepriseRepository;
import com.example.versuion.repository.RolesRepository;
import com.example.versuion.services.EntrepriseService;
import com.example.versuion.services.UtilisateurService;
import com.example.versuion.validator.EntrepriseValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EntrepriseServiceImpl implements EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurService utilisateurService;
    private final RolesRepository rolesRepository;
    private final String motDePasseAdminDefaut;

    public EntrepriseServiceImpl(EntrepriseRepository entrepriseRepository,
                                 RolesRepository rolesRepository, UtilisateurService utilisateurService,
                                 @Value("${app.entreprise.default-password:Admin123!}") String motDePasseAdminDefaut) {
        this.entrepriseRepository = entrepriseRepository;
        this.rolesRepository = rolesRepository;
        this.utilisateurService = utilisateurService;
        this.motDePasseAdminDefaut = motDePasseAdminDefaut;
    }

    @Override
    @Transactional
    public EntrepriseDto save(EntrepriseDto dto) {
        List<String> errors = EntrepriseValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Entreprise is not valid {}", dto);
            throw new InvalidEntityException("L'entreprise n'est pas valide", ErrorCodes.ENTREPRISE_NOT_VALID, errors);
        }
        EntrepriseDto savedEntreprise = EntrepriseDto.fromEntity(
                entrepriseRepository.save(EntrepriseDto.toEntity(dto))
        );

        UtilisateurDto utilisateurParDfaut = fromEntreprise(savedEntreprise);

        utilisateurParDfaut.setDateDeNaissance("pas encore");
        UtilisateurDto savedUser = utilisateurService.save(utilisateurParDfaut);

        RoleDto rolesDto = RoleDto.builder()
                .rolename("ADMIN")
                .utilisateur(savedUser)
                .build();

        rolesRepository.save(RoleDto.toEntity(rolesDto));

        // Renseigne le mot de passe temporaire UNIQUEMENT dans la réponse de création
        savedEntreprise.setMotDePasse(motDePasseAdminDefaut);
        log.info("Entreprise « {} » créée. Utilisateur admin : {}",
                savedEntreprise.getNom(), savedEntreprise.getEmail());

        return savedEntreprise;
    }

    private UtilisateurDto fromEntreprise(EntrepriseDto dto) {
        return UtilisateurDto.builder()
                .adresse(dto.getAdresse())
                .nom(dto.getNom())
                .prenom("Undefined")
                .email(dto.getEmail())
                .motDePasse(defaultAdminPassword())
                .entreprise(dto)
                .dateDeNaissance(null)
                .photo(dto.getPhoto())
                .build();
    }

    private String defaultAdminPassword() {
        // Mot de passe temporaire par défaut, surchargeable via la propriété
        // app.entreprise.default-password (env DEFAULT_ADMIN_PASSWORD).
        return motDePasseAdminDefaut;
    }

    @Override
    public EntrepriseDto findById(Long id) {
        if (id == null) {
            log.error("Entreprise ID is null");
            return null;
        }
        return entrepriseRepository.findByIdTenant(id)
                .map(EntrepriseDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune entreprise avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.ENTREPRISE_NOT_FOUND)
                );
    }

    @Override
    public List<EntrepriseDto> findAll() {
        return entrepriseRepository.findAllTenant().stream()
                .map(EntrepriseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Entreprise ID is null");
            return;
        }
        // Verifier que l'entreprise est bien celle de la requete courante
        entrepriseRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucune entreprise avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.ENTREPRISE_NOT_FOUND));
        entrepriseRepository.deleteById(id);
    }
}
