package com.example.versuion.services;

import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stockage des photos en LOCAL (remplace l'ancienne intégration Flickr).
 * Les fichiers sont écrits dans le répertoire configuré par {@code app.photos.dir}
 * (par défaut ./photos) et servis via {@code /photos/**} (voir WebMvcConfig).
 * Seules les images jpeg/png/webp sont acceptées, 5 Mo maximum.
 */
@Service
@Slf4j
public class PhotoStorageService {

    private static final long TAILLE_MAX = 5 * 1024 * 1024; // 5 Mo
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path repertoire;

    public PhotoStorageService(@Value("${app.photos.dir:./photos}") String repertoirePhotos) {
        this.repertoire = Paths.get(repertoirePhotos).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(repertoire);
        log.info("Stockage des photos : {}", repertoire);
    }

    /**
     * Enregistre une image et renvoie l'URL publique relative (ex. /photos/xxx.jpg).
     */
    public String enregistrer(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new InvalidOperationException("Aucun fichier fourni", ErrorCodes.ARTICLE_NOT_VALID);
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new InvalidOperationException("Image trop volumineuse (5 Mo maximum)", ErrorCodes.ARTICLE_NOT_VALID);
        }
        String extension = extension(fichier.getOriginalFilename());
        if (!EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException("Format acceptés : jpg, jpeg, png, webp", ErrorCodes.ARTICLE_NOT_VALID);
        }

        String nomFichier = UUID.randomUUID() + "." + extension;
        Path destination = repertoire.resolve(nomFichier);
        try (InputStream entree = fichier.getInputStream()) {
            Files.copy(entree, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Echec de l'enregistrement de la photo : {}", e.getMessage());
            throw new InvalidOperationException("Impossible d'enregistrer l'image", ErrorCodes.ARTICLE_NOT_VALID);
        }
        String url = "/photos/" + nomFichier;
        log.info("Photo enregistrée : {}", url);
        return url;
    }

    /**
     * Supprime une photo à partir de son URL publique (ignore les erreurs).
     */
    public void supprimer(String urlPhoto) {
        if (urlPhoto == null || !urlPhoto.startsWith("/photos/")) {
            return;
        }
        try {
            Files.deleteIfExists(repertoire.resolve(urlPhoto.substring("/photos/".length())));
        } catch (IOException e) {
            log.warn("Impossible de supprimer la photo {} : {}", urlPhoto, e.getMessage());
        }
    }

    private String extension(String nomFichier) {
        if (nomFichier == null || !nomFichier.contains(".")) {
            return "";
        }
        String e = nomFichier.substring(nomFichier.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        // jpg/jpeg traités comme des extensions distinctes, toutes deux autorisées
        return List.of("jpg", "jpeg", "png", "webp").contains(e) ? e : "";
    }
}
