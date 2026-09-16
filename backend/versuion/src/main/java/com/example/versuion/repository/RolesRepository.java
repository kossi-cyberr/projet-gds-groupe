package com.example.versuion.repository;

import com.example.versuion.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByRolenameAndUtilisateurId(String rolename, Long utilisateurId);

    List<Roles> findAllByUtilisateurId(Long utilisateurId);
}
