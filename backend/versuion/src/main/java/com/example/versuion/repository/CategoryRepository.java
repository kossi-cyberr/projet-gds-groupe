package com.example.versuion.repository;

import com.example.versuion.models.Category;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCodeCategory(String codeCategory);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<Category> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    Optional<Category> findByCodeCategoryAndIdEntreprise(String codeCategory, Integer idEntreprise);

    List<Category> findAllByIdEntreprise(Integer idEntreprise);

    default Optional<Category> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default Optional<Category> findByCodeCategoryTenant(String codeCategory) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByCodeCategoryAndIdEntreprise(codeCategory, idEntreprise) : findByCodeCategory(codeCategory);
    }

    default List<Category> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise) : findAll();
    }
}
