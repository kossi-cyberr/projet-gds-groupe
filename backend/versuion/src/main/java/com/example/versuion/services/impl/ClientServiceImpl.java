package com.example.versuion.services.impl;

import com.example.versuion.Dto.ClientDto;
import com.example.versuion.Dto.PageResponse;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.exception.InvalidEntityException;
import com.example.versuion.exception.InvalidOperationException;
import com.example.versuion.models.CommandeClient;
import com.example.versuion.repository.ClientRepository;
import com.example.versuion.repository.CommandeClientRepository;
import com.example.versuion.services.ClientService;
import com.example.versuion.utiles.CurrentEntreprise;
import com.example.versuion.utiles.PaginationUtils;
import com.example.versuion.validator.ClientValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final CommandeClientRepository commandeClientRepository;

    public ClientServiceImpl(ClientRepository clientRepository, CommandeClientRepository commandeClientRepository) {
        this.clientRepository = clientRepository;
        this.commandeClientRepository = commandeClientRepository;
    }

    @Override
    public ClientDto save(ClientDto dto) {
        List<String> errors = ClientValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Client is not valid {}", dto);
            throw new InvalidEntityException("Le client n'est pas valide", ErrorCodes.CLIENT_NOT_VALID, errors);
        }
        // Forcer l'entreprise courante (filtrage multi-entreprise)
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise != null) {
            dto.setIdEntreprise(idEntreprise);
        }
        return ClientDto.fromEntity(clientRepository.save(ClientDto.toEntity(dto)));
    }

    @Override
    public ClientDto update(Long id, ClientDto dto) {
        List<String> errors = ClientValidator.validate(dto);
        if (!errors.isEmpty()) {
            log.error("Client is not valid {}", dto);
            throw new InvalidEntityException("Le client n'est pas valide", ErrorCodes.CLIENT_NOT_VALID, errors);
        }
        ClientDto existant = findById(id);
        dto.setId(id);
        // Conserver le rattachement entreprise existant
        dto.setIdEntreprise(existant.getIdEntreprise());
        return ClientDto.fromEntity(clientRepository.save(ClientDto.toEntity(dto)));
    }

    @Override
    public ClientDto findById(Long id) {
        if (id == null) {
            log.error("Client ID is null");
            return null;
        }
        return clientRepository.findByIdTenant(id)
                .map(ClientDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun client avec l'ID " + id + " n'a ete trouve dans la base",
                        ErrorCodes.CLIENT_NOT_FOUND));
    }

    @Override
    public List<ClientDto> findAll() {
        return clientRepository.findAllTenant().stream()
                .map(ClientDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ClientDto> findAllPaginated(int page, int size, String sortBy, String sortDir, String search) {
        Pageable pageable = PaginationUtils.pageable(page, size, sortBy, sortDir,
                List.of("id", "nom", "prenom", "mail", "numTel"));
        Page<com.example.versuion.models.Client> result = StringUtils.hasLength(search)
                ? clientRepository.findAllTenant(search, pageable)
                : clientRepository.findAllTenant(pageable);
        return PageResponse.from(result, ClientDto::fromEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            log.error("Client ID is null");
            return;
        }
        // Verifier que le client appartient bien a l'entreprise courante avant suppression
        clientRepository.findByIdTenant(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Aucun client avec l'ID " + id + " n'a ete trouve dans la base",
                        ErrorCodes.CLIENT_NOT_FOUND));
        List<CommandeClient> commandeClients = commandeClientRepository.findAllByClientIdTenant(id);
        if (!commandeClients.isEmpty()) {
            throw new InvalidOperationException("Impossible de supprimer un client qui a deja des commande clients",
                    ErrorCodes.CLIENT_ALREADY_IN_USE);
        }
        clientRepository.deleteById(id);
    }
}
