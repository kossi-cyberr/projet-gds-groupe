package com.example.versuion.repository;

import com.example.versuion.models.Fournisseur;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<Fournisseur> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    List<Fournisseur> findAllByIdEntreprise(Integer idEntreprise);

    Page<Fournisseur> findAllByIdEntreprise(Integer idEntreprise, Pageable pageable);

    Page<Fournisseur> findAllByIdEntrepriseAndNomContainingIgnoreCase(Integer idEntreprise, String search, Pageable pageable);

    long countByIdEntreprise(Integer idEntreprise);

    default Optional<Fournisseur> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default List<Fournisseur> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise) : findAll();
    }

    default Page<Fournisseur> findAllTenant(Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise, pageable) : findAll(pageable);
    }

    default Page<Fournisseur> findAllTenant(String search, Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            return findAllByIdEntrepriseAndNomContainingIgnoreCase(idEntreprise, search, pageable);
        }
        return findAllByNomContainingIgnoreCase(search, pageable);
    }

    Page<Fournisseur> findAllByNomContainingIgnoreCase(String search, Pageable pageable);
}
