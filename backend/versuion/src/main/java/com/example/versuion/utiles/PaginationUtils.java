package com.example.versuion.utiles;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Construction sécurisée d'un {@link Pageable} à partir des paramètres HTTP.
 * Le champ de tri est restreint à une liste blanche pour éviter les erreurs
 * de résolution de propriété et les injections SQL via l'ordre de tri.
 */
public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 200;

    private PaginationUtils() {
    }

    public static Pageable pageable(int page, int size, String sortBy, String sortDir, List<String> allowedFields) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        String field = StringUtils.hasLength(sortBy) ? sortBy : "id";
        if (allowedFields != null && !allowedFields.isEmpty() && !allowedFields.contains(field)) {
            field = "id";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
