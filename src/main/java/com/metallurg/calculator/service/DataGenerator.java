package com.metallurg.calculator.service;

import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.SteelComposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Генерация случайных наборов входных данных. */
public class DataGenerator {

    private final Random random = new Random();

    private static final String[] GRADES = {
            "25Х2Н4МА", "25ХГСА", "40ХГНМ", "Ст3сп", "35ГС", "70", "40Х", "20ХН3А"
    };

    public List<AkosInput> generateAkos(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        List<AkosInput> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AkosInput in = new AkosInput();
            in.setSteelGrade(GRADES[random.nextInt(GRADES.length)]);
            in.setMeltMassKg(90_000 + random.nextInt(70_000));
            in.setSourceMode("random");
            SteelComposition before = new SteelComposition(
                    round3(0.10 + random.nextDouble() * 0.15),
                    round3(0.05 + random.nextDouble() * 0.2),
                    0,
                    round3(0.01 + random.nextDouble() * 0.01),
                    round3(0.01 + random.nextDouble() * 0.01),
                    round3(0.05 + random.nextDouble() * 0.15),
                    round3(0.2 + random.nextDouble() * 1.2),
                    round3(0.05 + random.nextDouble() * 0.08),
                    0
            );
            SteelComposition target = new SteelComposition(
                    round3(before.getC() + 0.08 + random.nextDouble() * 0.12),
                    round3(before.getMn() + 0.25 + random.nextDouble() * 0.5),
                    round3(0.2 + random.nextDouble() * 0.7),
                    0.02,
                    0.02,
                    round3(before.getCr() + 0.5 + random.nextDouble() * 0.8),
                    round3(before.getNi() + 0.4 + random.nextDouble() * 1.5),
                    round3(before.getMo() + 0.08 + random.nextDouble() * 0.15),
                    round3(0.03 + random.nextDouble() * 0.02)
            );
            in.setBefore(before);
            in.setTarget(target);
            list.add(in);
        }
        return list;
    }

    public List<MnlzInput> generateMnlz(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        List<MnlzInput> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MnlzInput in = new MnlzInput();
            in.setSteelGrade(GRADES[random.nextInt(GRADES.length)]);
            in.setConverterMassT(110 + random.nextInt(100));
            boolean slab = random.nextBoolean();
            if (slab) {
                in.setWidthM(round3(1.1 + random.nextDouble() * 0.5));
                in.setThicknessM(round3(0.15 + random.nextDouble() * 0.1));
                in.setCastingSpeedMPerMin(round3(0.9 + random.nextDouble() * 0.5));
            } else {
                double side = round3(0.1 + random.nextDouble() * 0.15);
                in.setWidthM(side);
                in.setThicknessM(side);
                in.setCastingSpeedMPerMin(round3(1.5 + random.nextDouble() * 2.5));
            }
            in.setCastingTimeMin(55 + random.nextInt(35));
            in.setSolidificationCoeff(MnlzCalculator.defaultSolidificationCoeff(in.getWidthM(), in.getThicknessM()));
            in.setSourceMode("random");
            list.add(in);
        }
        return list;
    }

    private static double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
