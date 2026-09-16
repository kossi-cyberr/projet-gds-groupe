package com.example.versuion.controller;

import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.controller.api.ClientApi;
import com.example.versuion.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClientControlleur implements ClientApi {

    ClientService clientService;

    @Autowired
    public ClientControlleur(ClientService clientService){
        this.clientService = clientService;
    }
    @Override
    public ClientDto save(ClientDto dto) {
        return clientService.save(dto);
    }

    @Override
    public ClientDto update(Long id, ClientDto dto) {
        return clientService.update(id, dto);
    }

    @Override
    public ClientDto findById(Long id) {
        return clientService.findById(id);
    }

    @Override
    public List<ClientDto> findAll() {
        return clientService.findAll();
    }

    @Override
    public PageResponse<ClientDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        return clientService.findAllPaginated(page, size, sortBy, sortDir, search);
    }

    @Override
    public void delete(Long id) {
        clientService.delete(id);
    }
}
