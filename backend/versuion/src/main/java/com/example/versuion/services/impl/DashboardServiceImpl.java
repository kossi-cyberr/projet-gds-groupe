package com.example.versuion.services.impl;

import com.example.versuion.Dto.dashboard.DashboardDto;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.ClientRepository;
import com.example.versuion.repository.CommandeClientRepository;
import com.example.versuion.repository.CommandeFournisseurRepository;
import com.example.versuion.repository.FournisseurRepository;
import com.example.versuion.repository.LigneVenteRepository;
import com.example.versuion.repository.VentesRepository;
import com.example.versuion.services.DashboardService;
import com.example.versuion.utiles.CurrentEntreprise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final LigneVenteRepository ligneVenteRepository;
    private final ArticleRepository articleRepository;
    private final ClientRepository clientRepository;
    private final FournisseurRepository fournisseurRepository;
    private final CommandeClientRepository commandeClientRepository;
    private final CommandeFournisseurRepository commandeFournisseurRepository;
    private final VentesRepository ventesRepository;

    public DashboardServiceImpl(LigneVenteRepository ligneVenteRepository,
                                ArticleRepository articleRepository,
                                ClientRepository clientRepository,
                                FournisseurRepository fournisseurRepository,
                                CommandeClientRepository commandeClientRepository,
                                CommandeFournisseurRepository commandeFournisseurRepository,
                                VentesRepository ventesRepository) {
        this.ligneVenteRepository = ligneVenteRepository;
        this.articleRepository = articleRepository;
        this.clientRepository = clientRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.commandeClientRepository = commandeClientRepository;
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.ventesRepository = ventesRepository;
    }

    @Override
    public DashboardDto getDashboard() {
        Integer idEntreprise = CurrentEntreprise.getId();
        if (idEntreprise == null) {
            log.warn("Dashboard demande sans entreprise courante");
            return DashboardDto.builder()
                    .chiffreAffairesTotal(BigDecimal.ZERO)
                    .chiffreAffairesMoisEnCours(BigDecimal.ZERO)
                    .chiffreAffairesJourEnCours(BigDecimal.ZERO)
                    .margeMoyenne(BigDecimal.ZERO)
                    .valeurStock(BigDecimal.ZERO)
                    .build();
        }

        LocalDate aujourdhui = LocalDate.now();

        return DashboardDto.builder()
                .chiffreAffairesTotal(ligneVenteRepository.chiffreAffairesTotal(idEntreprise))
                .chiffreAffairesMoisEnCours(ligneVenteRepository.chiffreAffairesMois(idEntreprise, aujourdhui.getYear(), aujourdhui.getMonthValue()))
                .chiffreAffairesJourEnCours(ligneVenteRepository.chiffreAffairesJour(idEntreprise, aujourdhui.getYear(), aujourdhui.getMonthValue(), aujourdhui.getDayOfMonth()))
                .nombreClients(clientRepository.countByIdEntreprise(idEntreprise))
                .nombreFournisseurs(fournisseurRepository.countByIdEntreprise(idEntreprise))
                .nombreArticles(articleRepository.countByIdEntreprise(idEntreprise))
                .nombreCommandesClient(commandeClientRepository.countByIdEntreprise(idEntreprise))
                .nombreCommandesFournisseur(commandeFournisseurRepository.countByIdEntreprise(idEntreprise))
                .nombreVentes(ventesRepository.countByIdEntreprise(idEntreprise))
                .stockSousSeuil(articleRepository.countArticlesSousSeuil(idEntreprise))
                .valeurStock(articleRepository.valeurStock(idEntreprise))
                .margeMoyenne(articleRepository.margeMoyenne(idEntreprise))
                .topArticles(ligneVenteRepository.topArticles(idEntreprise, PageRequest.of(0, 10)))
                .ventesParMois(ligneVenteRepository.ventesParMois(idEntreprise))
                .ventesParCategorie(ligneVenteRepository.ventesParCategorie(idEntreprise))
                .commandesParClient(commandeClientRepository.commandesParClient(idEntreprise))
                .build();
    }
}
