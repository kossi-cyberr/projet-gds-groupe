package com.example.versuion.services;

import com.example.versuion.Dto.ComandeClientDto;
import com.example.versuion.Dto.LigneCommandeClientDto;
import com.example.versuion.exception.EntityNotFoundException;
import com.example.versuion.exception.ErrorCodes;
import com.example.versuion.models.Entreprise;
import com.example.versuion.repository.EntrepriseRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Génère une facture PDF à partir d'une commande client
 * (en-tête entreprise, informations client, lignes et total TTC).
 */
@Service
@Slf4j
public class FacturePdfService {

    private static final Font TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
    private static final Font SOUS_TITRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font GRAS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

    private final CommandeClinetService commandeClinetService;
    private final EntrepriseRepository entrepriseRepository;

    public FacturePdfService(CommandeClinetService commandeClinetService,
                             EntrepriseRepository entrepriseRepository) {
        this.commandeClinetService = commandeClinetService;
        this.entrepriseRepository = entrepriseRepository;
    }

    public byte[] genererFacture(Long idCommandeClient) {
        ComandeClientDto commande = commandeClinetService.findById(idCommandeClient);
        List<LigneCommandeClientDto> lignes = commandeClinetService
                .findAllLignesCommandesClientByCommandeClientId(idCommandeClient);

        if (commande.getIdEntreprise() == null) {
            throw new EntityNotFoundException(
                    "La commande n'est pas rattachee a une entreprise, facture impossible",
                    ErrorCodes.ENTREPRISE_NOT_FOUND);
        }
        Entreprise entreprise = entrepriseRepository.findByIdTenant(commande.getIdEntreprise().longValue())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Entreprise introuvable pour la facture", ErrorCodes.ENTREPRISE_NOT_FOUND));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, entreprise);
            addCommandeInfo(document, commande);
            addClientInfo(document, commande);
            addLignes(document, lignes);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la generation de la facture pour la commande {}", idCommandeClient, e);
            throw new IllegalStateException("Impossible de generer la facture PDF", e);
        }
    }

    private void addHeader(Document document, Entreprise entreprise) {
        Paragraph titre = new Paragraph("FACTURE", TITRE);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph ent = new Paragraph();
        ent.setAlignment(Element.ALIGN_CENTER);
        ent.add(new Phrase(entreprise.getNom() + "\n", SOUS_TITRE));
        ent.add(new Phrase(entreprise.getEmail() + (entreprise.getSiteWeb() != null ? " - " + entreprise.getSiteWeb() : "") + "\n\n", NORMAL));
        document.add(ent);
    }

    private void addCommandeInfo(Document document, ComandeClientDto commande) {
        Paragraph info = new Paragraph();
        info.add(new Phrase("Commande n° : ", GRAS));
        info.add(new Phrase(commande.getCode() + "\n", NORMAL));
        if (commande.getDateComande() != null) {
            info.add(new Phrase("Date : ", GRAS));
            info.add(new Phrase(commande.getDateComande().atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n", NORMAL));
        }
        info.add(new Phrase("État : ", GRAS));
        info.add(new Phrase((commande.getEtatCommande() != null ? commande.getEtatCommande().name() : "—") + "\n\n", NORMAL));
        document.add(info);
    }

    private void addClientInfo(Document document, ComandeClientDto commande) {
        if (commande.getClient() == null) {
            return;
        }
        Paragraph client = new Paragraph();
        client.add(new Phrase("Client :\n", SOUS_TITRE));
        String nomComplet = (commande.getClient().getNom() != null ? commande.getClient().getNom() : "")
                + " " + (commande.getClient().getPrenom() != null ? commande.getClient().getPrenom() : "");
        client.add(new Phrase(nomComplet.trim() + "\n", NORMAL));
        if (commande.getClient().getAdresse() != null) {
            client.add(new Phrase(commande.getClient().getAdresse().getAddresse1() + "\n", NORMAL));
            client.add(new Phrase(commande.getClient().getAdresse().getVille()
                    + (commande.getClient().getAdresse().getCodePostale() != null ? " " + commande.getClient().getAdresse().getCodePostale() : "")
                    + (commande.getClient().getAdresse().getPays() != null ? " - " + commande.getClient().getAdresse().getPays() : "") + "\n", NORMAL));
        }
        if (commande.getClient().getMail() != null) {
            client.add(new Phrase(commande.getClient().getMail() + "\n", NORMAL));
        }
        if (commande.getClient().getNumTel() != null) {
            client.add(new Phrase(commande.getClient().getNumTel() + "\n\n", NORMAL));
        }
        document.add(client);
    }

    private void addLignes(Document document, List<LigneCommandeClientDto> lignes) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        addCell(table, "Désignation", true);
        addCell(table, "Qté", true);
        addCell(table, "PU HT", true);
        addCell(table, "TVA", true);
        addCell(table, "Total TTC", true);

        BigDecimal totalTtc = BigDecimal.ZERO;
        for (LigneCommandeClientDto ligne : lignes) {
            String designation = ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "—";
            String tva = ligne.getArticle() != null && ligne.getArticle().getTauxTva() != null
                    ? ligne.getArticle().getTauxTva().stripTrailingZeros().toPlainString() + " %"
                    : "—";
            BigDecimal prixTtc = ligne.getArticle() != null && ligne.getArticle().getPrixUnitaireTTc() != null
                    ? ligne.getArticle().getPrixUnitaireTTc()
                    : BigDecimal.ZERO;
            BigDecimal quantite = ligne.getQuantite() != null ? ligne.getQuantite() : BigDecimal.ZERO;
            BigDecimal totalLigne = prixTtc.multiply(quantite).setScale(2, RoundingMode.HALF_UP);
            totalTtc = totalTtc.add(totalLigne);

            addCell(table, designation, false);
            addCell(table, quantite.stripTrailingZeros().toPlainString(), false);
            addCell(table, ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire().toPlainString() : "", false);
            addCell(table, tva, false);
            addCell(table, totalLigne.toPlainString() + " FCFA", false);
        }

        document.add(table);

        Paragraph total = new Paragraph("TOTAL TTC : " + totalTtc.setScale(2, RoundingMode.HALF_UP).toPlainString() + " FCFA", TOTAL);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        document.add(total);
    }

    private void addCell(PdfPTable table, String text, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text, header ? GRAS : NORMAL));
        cell.setPadding(5);
        if (header) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        table.addCell(cell);
    }
}
