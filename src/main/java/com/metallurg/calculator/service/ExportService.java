package com.metallurg.calculator.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.metallurg.calculator.dao.CalculationDao;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Экспорт данных в CSV, XLSX и PDF. */
public class ExportService {

    public void exportMnlzCsv(Path path, List<CalculationDao.MnlzRow> rows) throws IOException {
        try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write('\ufeff');
            w.write("id;марка;садка_т;B_м;b_м;v_м/мин;τ_мин;ручьи;Lл_м;R_м;H_м;тип;режим;дата\n");
            for (CalculationDao.MnlzRow r : rows) {
                w.write(r.id() + ";" + r.steelGrade() + ";" + r.converterMassT() + ";"
                        + r.widthM() + ";" + r.thicknessM() + ";" + r.castingSpeed() + ";"
                        + r.castingTime() + ";" + r.strands() + ";" + r.liquidPool() + ";"
                        + r.radius() + ";" + r.height() + ";" + safe(r.machineType()) + ";"
                        + r.sourceMode() + ";" + r.createdAt() + "\n");
            }
        }
    }

    public void exportMnlzXlsx(Path path, List<CalculationDao.MnlzRow> rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("МНЛЗ");
            String[] headers = {"ID", "Марка", "Садка, т", "B, м", "b, м", "v, м/мин", "τ, мин",
                    "Ручьи", "Lл, м", "R, м", "H, м", "Тип", "Режим", "Дата"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowIdx = 1;
            for (CalculationDao.MnlzRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.id());
                row.createCell(1).setCellValue(r.steelGrade());
                row.createCell(2).setCellValue(r.converterMassT());
                row.createCell(3).setCellValue(r.widthM());
                row.createCell(4).setCellValue(r.thicknessM());
                row.createCell(5).setCellValue(r.castingSpeed());
                row.createCell(6).setCellValue(r.castingTime());
                row.createCell(7).setCellValue(r.strands());
                row.createCell(8).setCellValue(r.liquidPool());
                row.createCell(9).setCellValue(r.radius());
                row.createCell(10).setCellValue(r.height());
                row.createCell(11).setCellValue(r.machineType());
                row.createCell(12).setCellValue(r.sourceMode());
                row.createCell(13).setCellValue(r.createdAt());
            }
            try (OutputStream out = Files.newOutputStream(path)) {
                wb.write(out);
            }
        }
    }

    public void exportMnlzPdf(Path path, List<CalculationDao.MnlzRow> rows, String title)
            throws IOException, DocumentException {
        Document doc = new Document();
        PdfWriter.getInstance(doc, Files.newOutputStream(path));
        doc.open();
        Font font = russianFont(12);
        Font titleFont = russianFont(16);
        doc.add(new Paragraph(title, titleFont));
        doc.add(new Paragraph(" ", font));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        addCell(table, "ID", font);
        addCell(table, "Марка", font);
        addCell(table, "Садка, т", font);
        addCell(table, "Ручьи", font);
        addCell(table, "Lл, м", font);
        addCell(table, "R, м", font);
        for (CalculationDao.MnlzRow r : rows) {
            addCell(table, String.valueOf(r.id()), font);
            addCell(table, r.steelGrade(), font);
            addCell(table, String.valueOf(r.converterMassT()), font);
            addCell(table, String.valueOf(r.strands()), font);
            addCell(table, String.valueOf(r.liquidPool()), font);
            addCell(table, String.valueOf(r.radius()), font);
        }
        doc.add(table);
        doc.close();
    }

    public void exportTextReportPdf(Path path, String title, List<String> lines)
            throws IOException, DocumentException {
        Document doc = new Document();
        PdfWriter.getInstance(doc, Files.newOutputStream(path));
        doc.open();
        Font titleFont = russianFont(16);
        Font font = russianFont(12);
        doc.add(new Paragraph(title, titleFont));
        doc.add(new Paragraph(" ", font));
        for (String line : lines) {
            doc.add(new Paragraph(line, font));
        }
        doc.close();
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        table.addCell(new Paragraph(text == null ? "" : text, font));
    }

    private static Font russianFont(float size) throws DocumentException, IOException {
        // Используем встроенный Helvetica + Unicode через Identity-H где возможно;
        // для кириллицы пробуем системный шрифт macOS/Windows.
        String[] candidates = {
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
                "/Library/Fonts/Arial.ttf",
                "C:\\Windows\\Fonts\\arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String candidate : candidates) {
            Path p = Path.of(candidate);
            if (Files.exists(p)) {
                BaseFont bf = BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new Font(bf, size);
            }
        }
        return new Font(Font.HELVETICA, size);
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace(';', ',');
    }
}
