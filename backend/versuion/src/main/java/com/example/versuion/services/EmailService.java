package com.example.versuion.services;

import com.example.versuion.Dto.ArticleDto;
import com.example.versuion.models.Entreprise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Envoi des emails applicatifs (alertes de stock faible).
 * Si le SMTP n'est pas configuré ({@code spring.mail.host} vide) ou si
 * aucun destinataire n'est défini ({@code app.alert.email}), l'envoi est
 * simplement journalisé et ignoré — l'application ne doit jamais échouer
 * à cause de l'email.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String alertEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.alert.email:}") String alertEmail) {
        this.mailSender = mailSender;
        this.alertEmail = alertEmail;
    }

    /**
     * Envoie un email de relance listant les articles sous le seuil d'alerte.
     */
    public void envoyerAlerteStockFaible(Entreprise entreprise, List<ArticleDto> articlesSousSeuil) {
        if (articlesSousSeuil == null || articlesSousSeuil.isEmpty()) {
            return;
        }
        if (!StringUtils.hasLength(alertEmail)) {
            log.info("Alerte stock faible non envoyee : aucun destinataire configure (app.alert.email). " +
                    "Articles sous seuil pour [{}] : {}", entreprise.getNom(), articlesSousSeuil.size());
            return;
        }

        String corps = articlesSousSeuil.stream()
                .map(a -> "- " + a.getCodeArticle() + " : " + a.getDesignation()
                        + " (seuil : " + a.getSeuilAlerte() + ")")
                .collect(Collectors.joining("\n"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(alertEmail);
        message.setSubject("[Gestion de Stock] Alerte : articles sous le seuil - " + entreprise.getNom());
        message.setText("Bonjour,\n\nLes articles suivants sont sous leur seuil d'alerte pour l'entreprise '"
                + entreprise.getNom() + "' :\n\n" + corps + "\n\nCordialement,\nL'application Gestion de Stock");

        try {
            mailSender.send(message);
            log.info("Alerte stock faible envoyee a {} pour l'entreprise {}", alertEmail, entreprise.getNom());
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'alerte stock faible a {} : {}", alertEmail, e.getMessage());
        }
    }
}
