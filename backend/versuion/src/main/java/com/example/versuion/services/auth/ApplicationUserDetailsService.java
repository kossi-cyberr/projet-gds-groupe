package com.example.versuion.services.auth;

import com.example.versuion.Dto.UtilisateurDto;
import com.example.versuion.models.auth.ExtendedUser;
import com.example.versuion.services.UtilisateurService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//Chercher un utilisateur par son email
@Service
@Slf4j
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UtilisateurService utilisateurService;

    public ApplicationUserDetailsService(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UtilisateurDto utilisateurs = utilisateurService.findByEmail(email);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (utilisateurs.getRoles() != null) {
            utilisateurs.getRoles().forEach(
                    role -> authorities.add(new SimpleGrantedAuthority(role.getRolename()))
            );
        }

        Long idEntreprise = utilisateurs.getEntreprise() != null ? utilisateurs.getEntreprise().getId() : null;
        return new ExtendedUser(utilisateurs.getEmail(), utilisateurs.getMotDePasse(), idEntreprise, authorities);
    }
}
