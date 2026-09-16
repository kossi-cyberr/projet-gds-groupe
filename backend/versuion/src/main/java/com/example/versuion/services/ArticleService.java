package com.example.versuion.services;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.Dto.LigneCommandeFournisseurDto;
import com.example.versuion.Dto.LigneVentDto;
import com.example.versuion.Dto.PageResponse;

import java.util.List;

public interface ArticleService {

    ArticleDto save(ArticleDto articleDto);

    ArticleDto findById(Long articleId);

    ArticleDto findByCodeArticle(String codeArticle);

    List<ArticleDto> findAll();

    PageResponse<ArticleDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search);

    List<ArticleDto> findArticlesSousSeuil();

    void delete (Long articleId);

    List<LigneVentDto> findHistoriqueVentes(Long idArticle);

    List<LigneCommandeClientDto> findHistoriqueCommandeClient(Long idArticle);

    List<LigneCommandeFournisseurDto> findHistoriqueCommandeFournisseur(Long idArticle);

    List<ArticleDto> findAllArticleByIdCategory(Long idCategory);


}
