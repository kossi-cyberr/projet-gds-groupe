package com.example.versuion.jwt;

import com.example.versuion.services.auth.ApplicationUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.versuion.utiles.CurrentEntreprise.MDC_ID_ENTREPRISE;

// Ce filtre va intercepter les requetes
@Component
@Slf4j
public class JwtRequestFiltre extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ApplicationUserDetailsService applicationUserDetailsService;

    public JwtRequestFiltre(JwtUtil jwtUtil, ApplicationUserDetailsService applicationUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.applicationUserDetailsService = applicationUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            String userEmail = null;
            String jwt = null;
            String idEntreprise = null;

            // Verifier si l authorization header exicte et n'est pas null
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    jwt = authorizationHeader.substring(7);
                    userEmail = jwtUtil.extractUsername(jwt);
                    idEntreprise = jwtUtil.extractIdEntreprise(jwt);
                } catch (JwtException | IllegalArgumentException e) {
                    // Token invalide/expire : la requete reste non authentifiee (401)
                    log.warn("Token JWT invalide : {}", e.getMessage());
                }
            }

            // Verifier si j'ai un utilisateur :
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = applicationUserDetailsService.loadUserByUsername(userEmail);

                //Verifier que le token est appartient pour cette utilisateur
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            // Stocker l'id entreprise pour le filtrage multi-entreprise (uniquement si présent)
            if (idEntreprise != null) {
                MDC.put(MDC_ID_ENTREPRISE, idEntreprise);
            }

            //contenir les restes des filtres
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_ID_ENTREPRISE);
        }
    }
}
