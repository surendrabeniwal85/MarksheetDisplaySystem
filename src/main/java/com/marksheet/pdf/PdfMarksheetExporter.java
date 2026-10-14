package com.marksheet.pdf;

import com.marksheet.model.Marksheet;
import com.marksheet.model.Subject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class PdfMarksheetExporter {
    private static final float LEFT = 42, RIGHT = 553;
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public Path export(Marksheet m, Path destination) throws IOException {
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream s = new PDPageContentStream(document, page)) {
                // Custom academic emblem, not an institutional logo.
                s.setLineWidth(1.5f);
                s.addRect(48, 786, 38, 38); s.stroke();
                textCentered(s, BOLD, 20, 67, 798, "A");
                text(s, BOLD, 13, 98, 811, fit(m.institutionName(), 48));
                text(s, REGULAR, 8, 98, 798, "ACADEMIC GRADE SUMMARY");
                line(s, LEFT, 782, RIGHT, 782);

                textCentered(s, BOLD, 14, 297, 758, "GRADE CARD");
                textCentered(s, BOLD, 11, 297, 738, fit(m.institutionName(), 65));
                textCentered(s, BOLD, 11, 297, 722, fit(m.programme(), 65));
                textCentered(s, BOLD, 11, 297, 706, fit(m.semester(), 65));

                text(s, BOLD, 9, LEFT, 676, "NAME: " + fit(m.studentName(), 32));
                text(s, BOLD, 9, 380, 676, "REG. NO.: " + fit(m.registrationNumber(), 20));
                text(s, BOLD, 9, LEFT, 657, "FATHER/GUARDIAN: " + fit(m.fatherName(), 42));

                float top = 638, rh = 25;
                float[] x = {LEFT, 145, 430, 485, RIGHT};
                for (int c = 0; c < 4; c++) drawCell(s, x[c], top - rh, x[c+1] - x[c], rh);
                text(s, BOLD, 8, x[0] + 5, top - 16, "SUBJECT CODE");
                text(s, BOLD, 8, x[1] + 5, top - 16, "SUBJECT TITLE");
                textCentered(s, BOLD, 8, (x[2] + x[3]) / 2, top - 16, "CREDIT");
                textCentered(s, BOLD, 8, (x[3] + x[4]) / 2, top - 16, "GRADE");

                float y = top - rh;
                for (Subject subject : m.subjects()) {
                    y -= rh;
                    for (int c = 0; c < 4; c++) drawCell(s, x[c], y, x[c+1] - x[c], rh);
                    text(s, REGULAR, 8, x[0] + 5, y + 9, fit(subject.code(), 15));
                    text(s, REGULAR, 8, x[1] + 5, y + 9, fit(subject.title(), 52));
                    textCentered(s, REGULAR, 8, (x[2] + x[3]) / 2, y + 9, "" + subject.credits());
                    textCentered(s, REGULAR, 8, (x[3] + x[4]) / 2, y + 9, fit(subject.grade(), 5));
                }
                y -= rh;
                for (int c = 0; c < 4; c++) drawCell(s, x[c], y, x[c+1] - x[c], rh);
                text(s, BOLD, 9, x[0] + 5, y + 9, "TOTAL");
                textCentered(s, BOLD, 9, (x[2] + x[3]) / 2, y + 9, "" + m.totalCredits());
                textCentered(s, BOLD, 9, (x[3] + x[4]) / 2, y + 9, "---");

                text(s, BOLD, 10, LEFT + 10, y - 30, String.format("SGPA: %.2f", m.sgpa()));
                text(s, BOLD, 10, 365, y - 30, "CREDITS EARNED: " + m.totalCredits());
                textCentered(s, BOLD, 10, 297, y - 65, "UNOFFICIAL GRADE SUMMARY");
                textCentered(s, REGULAR, 8, 297, y - 79, "Not issued or verified by the named institution");

                line(s, LEFT, 35, RIGHT, 35);
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
                text(s, REGULAR, 8, LEFT, 22, "Generated: " + date);
            }
            document.save(destination.toFile());
        }
        return destination;
    }

    private static void drawCell(PDPageContentStream s, float x, float y, float w, float h) throws IOException {
        s.addRect(x, y, w, h); s.stroke();
    }
    private static void line(PDPageContentStream s, float x1, float y1, float x2, float y2) throws IOException {
        s.moveTo(x1, y1); s.lineTo(x2, y2); s.stroke();
    }
    private static void text(PDPageContentStream s, PDType1Font font, float size,
                             float x, float y, String value) throws IOException {
        s.beginText(); s.setFont(font, size); s.newLineAtOffset(x, y); s.showText(value); s.endText();
    }
    private static void textCentered(PDPageContentStream s, PDType1Font font, float size,
                                     float centerX, float y, String value) throws IOException {
        float width = font.getStringWidth(value) / 1000 * size;
        text(s, font, size, centerX - width / 2, y, value);
    }
    private static String fit(String value, int max) {
        String safe = value == null ? "" : value.replaceAll("[^\\x20-\\x7E]", "?");
        return safe.length() <= max ? safe : safe.substring(0, max - 1) + "…";
    }
}
