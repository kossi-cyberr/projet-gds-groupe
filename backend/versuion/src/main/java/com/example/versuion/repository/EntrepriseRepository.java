package com.example.versuion.repository;

import com.example.versuion.models.Entreprise;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    @Query("select e from Entreprise e where e.id = :idEntreprise")
    List<Entreprise> findAllByIdEntreprise(@Param("idEntreprise") Integer idEntreprise);

    @Query("select e from Entreprise e where e.id = :id and e.id = :idEntreprise")
    Optional<Entreprise> findByIdAndIdEntreprise(@Param("id") Long id, @Param("idEntreprise") Integer idEntreprise);

    default Optional<Entreprise> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default List<Entreprise> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise) : findAll();
    }
}
