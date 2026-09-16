package com.example.versuion.repository;

import com.example.versuion.models.Client;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<Client> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    List<Client> findAllByIdEntreprise(Integer idEntreprise);

    Page<Client> findAllByIdEntreprise(Integer idEntreprise, Pageable pageable);

    Page<Client> findAllByIdEntrepriseAndNomContainingIgnoreCase(Integer idEntreprise, String search, Pageable pageable);

    long countByIdEntreprise(Integer idEntreprise);

    default Optional<Client> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default List<Client> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise) : findAll();
    }

    default Page<Client> findAllTenant(Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise, pageable) : findAll(pageable);
    }

    default Page<Client> findAllTenant(String search, Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            return findAllByIdEntrepriseAndNomContainingIgnoreCase(idEntreprise, search, pageable);
        }
        return findAllByNomContainingIgnoreCase(search, pageable);
    }

    Page<Client> findAllByNomContainingIgnoreCase(String search, Pageable pageable);
}
