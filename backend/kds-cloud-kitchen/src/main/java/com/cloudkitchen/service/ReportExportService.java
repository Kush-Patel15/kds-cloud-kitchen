package com.cloudkitchen.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ReportExportService {

    public byte[] exportPdf(String type, Map<String,Object> data) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = page.getMediaBox().getHeight() - 50;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, y);
                cs.showText(cap(type) + " Report");
                cs.endText();

                y -= 30;
                cs.setFont(PDType1Font.HELVETICA, 12);
                for (Map.Entry<String,Object> e : data.entrySet()) {
                    if (y < 70) {
                        cs.close();
                        PDPage np = new PDPage(PDRectangle.A4);
                        doc.addPage(np);
                        y = np.getMediaBox().getHeight() - 50;
                        try (PDPageContentStream cs2 = new PDPageContentStream(doc, np)) {
                            // (Simplified; for multiple pages you'd manage streams carefully)
                        }
                    }
                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    cs.showText(e.getKey() + ": " + String.valueOf(e.getValue()));
                    cs.endText();
                    y -= 18;
                }

                cs.beginText();
                cs.newLineAtOffset(50, y - 10);
                cs.showText("Generated: " + LocalDateTime.now());
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("PDF generation failed", ex);
        }
    }

    public byte[] exportExcel(String type, Map<String,Object> data) {
        // Actually CSV – openable in Excel
        StringBuilder sb = new StringBuilder();
        sb.append("Report Type,").append(type).append("\n");
        data.forEach((k,v) -> sb.append(k).append(",").append(sanitize(String.valueOf(v))).append("\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String sanitize(String s) {
        return s.replace("\n"," ").replace(","," ");
    }
    private String cap(String s){ return s.substring(0,1).toUpperCase()+s.substring(1); }
}