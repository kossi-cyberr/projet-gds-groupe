package com.example.versuion.controller;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.services.ArticleService;
import com.example.versuion.services.PhotoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload de photos stockées localement (remplace l'ancienne intégration Flickr).
 * POST /gestiondestock/photos/article/{id}  : enregistre l'image et la rattache à l'article.
 */
@RestController
@RequestMapping("/gestiondestock/photos")
@Tag(name = "Photos", description = "Upload des photos (stockage local)")
public class PhotoControlleur {

    private final PhotoStorageService photoStorageService;
    private final ArticleService articleService;

    public PhotoControlleur(PhotoStorageService photoStorageService, ArticleService articleService) {
        this.photoStorageService = photoStorageService;
        this.articleService = articleService;
    }

    @PostMapping("/article/{idArticle}")
    @Operation(summary = "Uploader la photo d'un article (jpg/png/webp, 5 Mo max)")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ArticleDto uploadPhotoArticle(@PathVariable Long idArticle,
                                         @RequestParam("file") MultipartFile fichier) {
        String url = photoStorageService.enregistrer(fichier);
        ArticleDto article = articleService.findById(idArticle);
        String ancienne = article.getPhoto();
        article.setPhoto(url);
        ArticleDto misAJour = articleService.save(article); // save() fait l'upsert (id présent)
        if (ancienne != null && !ancienne.equals(url)) {
            photoStorageService.supprimer(ancienne);
        }
        return misAJour;
    }
}
