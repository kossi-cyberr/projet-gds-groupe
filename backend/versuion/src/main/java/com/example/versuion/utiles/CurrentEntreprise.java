package com.example.versuion.utiles;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * Fournit l'identifiant de l'entreprise courante (rempli par {@code JwtRequestFiltre}
 * à partir du token JWT). Retourne {@code null} lorsqu'aucune entreprise n'est
 * associée à la requête (endpoints publics par exemple).
 */
public final class CurrentEntreprise {

    public static final String MDC_ID_ENTREPRISE = "idEntreprise";

    private CurrentEntreprise() {
    }

    public static Integer getId() {
        final String idEntreprise = MDC.get(MDC_ID_ENTREPRISE);
        if (!StringUtils.hasLength(idEntreprise)) {
            return null;
        }
        try {
            return Integer.valueOf(idEntreprise);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
