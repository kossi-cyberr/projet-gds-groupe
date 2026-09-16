package com.example.versuion.services;

import com.example.versuion.Dto.FournisseurDto;
import com.example.versuion.Dto.PageResponse;

import java.util.List;

public interface FournisseurService {

    FournisseurDto save(FournisseurDto dto);

    FournisseurDto update(Long id, FournisseurDto dto);

    FournisseurDto findById(Long id);

    List<FournisseurDto> findAll();

    PageResponse<FournisseurDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search);

    void delete(Long id);
}
