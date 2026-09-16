package com.example.versuion.services.impl;

import com.example.versuion.Dto.ChangerMotDePasseUtilisateurDto;
import com.example.versuion.Dto.EntrepriseDto;
import com.example.versuion.Dto.UtilisateurDto;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.Roles;
import com.example.versuion.models.Utilisateurs;
import com.example.versuion.repository.RolesRepository;
import com.example.versuion.repository.UtilisateurRepository;
import com.example.versuion.services.PhotoStorageService;
import com.example.versuion.services.UtilisateurService;
import com.example.versuion.validator.UtilisateursValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.versuion.utiles.Constants.ROLE_ADMIN;
import static com.example.versuion.utiles.Constants.ROLE_MANAGER;
import static com.example.versuion.utiles.Constants.ROLE_VENDEUR;

@Service
@Slf4j
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhotoStorageService photoStorageService;

    private static final List<String> ROLES_AUTORISES = List.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_VENDEUR);

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  RolesRepository rolesRepository,
                                  @Lazy PasswordEncoder passwordEncoder,
                                  PhotoStorageService photoStorageService) {
        this.utilisateurRepository = utilisateurRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.photoStorageService = photoStorageService;
    }

    @Override
    @Transactional
    public UtilisateurDto save(UtilisateurDto dto) {
        List<String> errors = UtilisateursValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Utilisateur is not valid {}", dto);
            throw new InvalidEntityException("L'utilisateur n'est pas valide", ErrorCodes.UTILISATEUR_NOT_VALID, errors);
        }
        if (userAlreadyExists(dto.getEmail())) {
            throw new InvalidEntityException("Un autre utilisateur avec le meme email existe deja", ErrorCodes.UTILISATEUR_ALREADY_EXISTS,
                    Collections.singletonList("Un autre utilisateur avec le meme email existe deja dans la BDD"));
        }

        // Forcer l'entreprise courante (filtrage multi-entreprise) : l'utilisateur
        // est toujours rattaché à l'entreprise de l'administrateur connecté.
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setEntreprise(EntrepriseDto.builder().id(idEntreprise.longValue()).build());
        }

        dto.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        return UtilisateurDto.fromEntity(
                utilisateurRepository.save(UtilisateurDto.toEntity(dto))
        );
    }

    private boolean userAlreadyExists(String email) {
        Optional<Utilisateurs> user = utilisateurRepository.findByEmail(email);
        return user.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto findById(Long id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            return null;
        }
        return utilisateurRepository.findByIdTenant(id)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> findAll() {
        return utilisateurRepository.findAllTenant().stream()
                .map(UtilisateurDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Utilisateur ID is null");
            return;
        }
        // Verifier que l'utilisateur appartient bien a l'entreprise courante avant suppression
        utilisateurRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'ID = " + id + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));
        utilisateurRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto findByEmail(String email) {
        return utilisateurRepository.findByEmailTenant(email)
                .map(UtilisateurDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'email = " + email + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND)
                );
    }

    @Override
    @Transactional
    public UtilisateurDto assignerRole(Long idUtilisateur, String roleName) {
        if (idUtilisateur == null || !StringUtils.hasLength(roleName)) {
            log.warn("Impossible d'affecter un role avec un ID utilisateur ou un nom de role vide");
            throw new InvalidOperationException("ID utilisateur ou nom de role manquant",
                    ErrorCodes.UTILISATEUR_NOT_VALID);
        }

        String nomRole = roleName.trim().toUpperCase(Locale.ROOT);
        if (!ROLES_AUTORISES.contains(nomRole)) {
            log.warn("Role inconnu : {}", roleName);
            throw new InvalidOperationException("Le role '" + roleName + "' n'existe pas. Roles disponibles : " + ROLES_AUTORISES,
                    ErrorCodes.UTILISATEUR_NOT_VALID);
        }

        Utilisateurs utilisateur = utilisateurRepository.findByIdTenant(idUtilisateur)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'ID = " + idUtilisateur + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        Optional<Roles> roleExistant = rolesRepository.findByRolenameAndUtilisateurId(nomRole, idUtilisateur);
        if (roleExistant.isPresent()) {
            throw new InvalidOperationException("Le role " + nomRole + " est deja affecte a cet utilisateur",
                    ErrorCodes.UTILISATEUR_ALREADY_EXISTS);
        }

        Roles role = new Roles();
        role.setRolename(nomRole);
        role.setUtilisateur(utilisateur);
        rolesRepository.save(role);

        return findById(idUtilisateur);
    }

    @Override
    @Transactional
    public UtilisateurDto changerMotDePasse(ChangerMotDePasseUtilisateurDto dto) {
        validate(dto);
        Utilisateurs utilisateur = utilisateurRepository.findByIdTenant(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur n'a ete trouve avec l'ID " + dto.getId(),
                        ErrorCodes.UTILISATEUR_NOT_FOUND));

        // Sécurité : un utilisateur ne peut modifier que SON PROPRE mot de passe,
        // sauf l'ADMIN qui peut le faire pour n'importe quel utilisateur.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailCourant = authentication != null ? authentication.getName() : null;
        boolean estAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));

        if (!estAdmin && !utilisateur.getEmail().equals(emailCourant)) {
            throw new InvalidOperationException(
                    "Vous ne pouvez modifier que votre propre mot de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        return UtilisateurDto.fromEntity(
                utilisateurRepository.save(utilisateur)
        );
    }

    @Override
    @Transactional
    public UtilisateurDto updateMonProfil(UtilisateurDto dto) {
        Utilisateurs utilisateur = utilisateurConnecte();

        // Champs modifiables depuis la page profil
        if (dto.getNom() != null) {
            utilisateur.setNom(dto.getNom());
        }
        if (dto.getPrenom() != null) {
            utilisateur.setPrenom(dto.getPrenom());
        }
        if (dto.getDateDeNaissance() != null) {
            utilisateur.setDateDeNaissance(dto.getDateDeNaissance());
        }
        if (dto.getAdresse() != null) {
            if (utilisateur.getAdresse() == null) {
                utilisateur.setAdresse(new com.example.versuion.models.Adresse());
            }
            utilisateur.getAdresse().setAddresse1(dto.getAdresse().getAddresse1());
            utilisateur.getAdresse().setAddresse2(dto.getAdresse().getAddresse2());
            utilisateur.getAdresse().setVille(dto.getAdresse().getVille());
            utilisateur.getAdresse().setCodePostale(dto.getAdresse().getCodePostale());
            utilisateur.getAdresse().setPays(dto.getAdresse().getPays());
        }

        return UtilisateurDto.fromEntity(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional
    public UtilisateurDto updateMaPhoto(String urlPhoto) {
        Utilisateurs utilisateur = utilisateurConnecte();
        String ancienne = utilisateur.getPhoto();
        utilisateur.setPhoto(urlPhoto);
        UtilisateurDto resultat = UtilisateurDto.fromEntity(utilisateurRepository.save(utilisateur));
        // Supprimer l'ancienne photo du disque si remplacée
        if (ancienne != null && !ancienne.equals(urlPhoto)) {
            photoStorageService.supprimer(ancienne);
        }
        return resultat;
    }

    /**
     * Renvoie l'entité de l'utilisateur connecté (à partir du token JWT),
     * indépendamment du filtrage multi-entreprise.
     */
    private Utilisateurs utilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        if (!StringUtils.hasLength(email)) {
            throw new InvalidOperationException("Aucun utilisateur connecté", ErrorCodes.UTILISATEUR_NOT_FOUND);
        }
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun utilisateur avec l'email = " + email + " n'a ete trouve dans la BDD",
                        ErrorCodes.UTILISATEUR_NOT_FOUND));
    }

    private void validate(ChangerMotDePasseUtilisateurDto dto) {
        if (dto == null) {
            log.warn("Impossible de modifier le mot de passe avec un objet NULL");
            throw new InvalidOperationException("Aucune information n'a ete fourni pour pouvoir changer le mot de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (dto.getId() == null) {
            log.warn("Impossible de modifier le mot de passe avec un ID NULL");
            throw new InvalidOperationException("ID utilisateur null:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (!StringUtils.hasLength(dto.getMotDePasse()) || !StringUtils.hasLength(dto.getConfirmMotDePasse())) {
            log.warn("Impossible de modifier le mot de passe avec un mot de passe NULL");
            throw new InvalidOperationException("Mot de passe utilisateur null:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
            log.warn("Impossible de modifier le mot de passe avec deux mots de passe different");
            throw new InvalidOperationException("Mots de passe utilisateur non conformes:: Impossible de modifier le mote de passe",
                    ErrorCodes.UTILISATEUR_CHANGE_PASSWORD_OBJECT_NOT_VALID);
        }
    }
}
