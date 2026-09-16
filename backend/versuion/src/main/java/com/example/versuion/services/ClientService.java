package com.example.versuion.services;

import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.PageResponse;

import java.util.List;

public interface ClientService {

    ClientDto save(ClientDto dto);

    ClientDto update(Long id, ClientDto dto);

    ClientDto findById(Long id);

    List<ClientDto> findAll();

    PageResponse<ClientDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search);

    void delete(Long id);

}
