package com.metallurg.calculator.service;

import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.SteelComposition;
import com.metallurg.calculator.util.Validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Импорт входных данных из CSV.
 * АКОС: grade;massKg;beforeC;beforeMn;...;targetC;targetMn;...
 * МНЛЗ: grade;massT;width;thickness;speed;time;[kz]
 */
public class FileImportService {

    public List<AkosInput> importAkos(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<AkosInput> result = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().startsWith("grade")) {
                continue;
            }
            String[] p = trimmed.split("[;,]");
            if (p.length < 20) {
                throw new IllegalArgumentException("Строка АКОС должна содержать 20 полей: " + trimmed);
            }
            AkosInput in = new AkosInput();
            in.setSteelGrade(Validation.requireText(p[0], "марка"));
            in.setMeltMassKg(Validation.requirePositive(p[1], "масса"));
            in.setBefore(readComposition(p, 2));
            in.setTarget(readComposition(p, 11));
            in.setSourceMode("file");
            result.add(in);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Файл не содержит строк данных АКОС");
        }
        return result;
    }

    public List<MnlzInput> importMnlz(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<MnlzInput> result = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().startsWith("grade")) {
                continue;
            }
            String[] p = trimmed.split("[;,]");
            if (p.length < 6) {
                throw new IllegalArgumentException("Строка МНЛЗ должна содержать ≥6 полей: " + trimmed);
            }
            MnlzInput in = new MnlzInput();
            in.setSteelGrade(Validation.requireText(p[0], "марка"));
            in.setConverterMassT(Validation.requirePositive(p[1], "садка"));
            in.setWidthM(Validation.requirePositive(p[2], "ширина"));
            in.setThicknessM(Validation.requirePositive(p[3], "толщина"));
            in.setCastingSpeedMPerMin(Validation.requirePositive(p[4], "скорость"));
            in.setCastingTimeMin(Validation.requirePositive(p[5], "время"));
            if (p.length >= 7 && !p[6].isBlank()) {
                in.setSolidificationCoeff(Validation.requirePositive(p[6], "kз"));
            } else {
                in.setSolidificationCoeff(MnlzCalculator.defaultSolidificationCoeff(in.getWidthM(), in.getThicknessM()));
            }
            in.setSourceMode("file");
            result.add(in);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Файл не содержит строк данных МНЛЗ");
        }
        return result;
    }

    private SteelComposition readComposition(String[] p, int offset) {
        return new SteelComposition(
                parse(p[offset], "C"),
                parse(p[offset + 1], "Mn"),
                parse(p[offset + 2], "Si"),
                parse(p[offset + 3], "S"),
                parse(p[offset + 4], "P"),
                parse(p[offset + 5], "Cr"),
                parse(p[offset + 6], "Ni"),
                parse(p[offset + 7], "Mo"),
                parse(p[offset + 8], "Al")
        );
    }

    private double parse(String raw, String name) {
        return Validation.requireNonNegative(raw, name);
    }
}
