package com.example.versuion.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exports génériques : classeur Excel (XLSX via Apache POI) et CSV
 * (séparateur « ; » pour la compatibilité Excel en locale française).
 */
@Service
@Slf4j
public class ExportService {

    public byte[] exporterExcel(String sheetName, String[] headers, List<List<Object>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (List<Object> row : rows) {
                Row r = sheet.createRow(rowIndex++);
                for (int i = 0; i < headers.length; i++) {
                    Cell c = r.createCell(i);
                    Object value = i < row.size() ? row.get(i) : null;
                    if (value == null) {
                        c.setCellValue("");
                    } else if (value instanceof BigDecimal bd) {
                        c.setCellValue(bd.doubleValue());
                    } else if (value instanceof Number number) {
                        c.setCellValue(number.doubleValue());
                    } else {
                        c.setCellValue(value.toString());
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel", e);
            throw new IllegalStateException("Impossible de generer le fichier Excel", e);
        }
    }

    public byte[] exporterCsv(String[] headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(";", headers)).append("\r\n");
        for (List<Object> row : rows) {
            sb.append(row.stream()
                    .map(v -> v == null ? "" : escapeCsv(String.valueOf(v)))
                    .collect(Collectors.joining(";")))
              .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
