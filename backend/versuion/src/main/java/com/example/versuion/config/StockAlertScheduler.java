package com.example.versuion.config;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.models.Entreprise;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.example.versuion.repository.ArticleRepository;
import com.example.versuion.repository.EntrepriseRepository;
import com.example.versuion.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vérifie chaque jour (8h par défaut, configurable via {@code app.stock-alert.cron})
 * le niveau de stock de chaque entreprise et envoie un email d'alerte si des
 * articles sont sous leur seuil.
 */
@Component
@Slf4j
public class StockAlertScheduler {

    private final EntrepriseRepository entrepriseRepository;
    private final ArticleRepository articleRepository;
    private final EmailService emailService;
    private final String smtpHost;
    private final String alertEmail;

    public StockAlertScheduler(EntrepriseRepository entrepriseRepository,
                               ArticleRepository articleRepository,
                               EmailService emailService,
                               @Value("${spring.mail.host:}") String smtpHost,
                               @Value("${app.alert.email:}") String alertEmail) {
        this.entrepriseRepository = entrepriseRepository;
        this.articleRepository = articleRepository;
        this.emailService = emailService;
        this.smtpHost = smtpHost;
        this.alertEmail = alertEmail;
    }

    /**
     * Avertit explicitement au démarrage si les alertes email ne peuvent pas partir
     * (SMTP ou destinataire non configuré) — évite les silences trompeurs.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void signalerConfigurationEmail() {
        if (!StringUtils.hasLength(smtpHost) || "localhost".equals(smtpHost)) {
            log.warn("=== ALERTES EMAIL DESACTIVÉES : SMTP non configuré " +
                    "(définir SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD). " +
                    "Les vérifications quotidiennes seront seulement journalisées. ===");
        } else if (!StringUtils.hasLength(alertEmail)) {
            log.warn("=== ALERTES EMAIL DESACTIVÉES : aucun destinataire (définir ALERT_EMAIL). ===");
        } else {
            log.info("Alertes email de stock activées vers {} via {}", alertEmail, smtpHost);
        }
    }

    @Scheduled(cron = "${app.stock-alert.cron:0 0 8 * * *}")
    public void verifierStocksSousSeuil() {
        log.info("Verification planifiee des stocks sous seuil...");
        List<Entreprise> entreprises = entrepriseRepository.findAll();
        for (Entreprise entreprise : entreprises) {
            try {
                List<ArticleDto> sousSeuil = articleRepository
                        .findArticlesSousSeuil(entreprise.getId().intValue())
                        .stream()
                        .map(ArticleDto::fromEntity)
                        .collect(Collectors.toList());
                emailService.envoyerAlerteStockFaible(entreprise, sousSeuil);
            } catch (Exception e) {
                log.warn("Echec de la verification des stocks pour l'entreprise {} : {}",
                        entreprise.getNom(), e.getMessage());
            }
        }
    }
}
