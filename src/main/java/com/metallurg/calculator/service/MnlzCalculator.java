package com.metallurg.calculator.service;

import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.MnlzResult;

/**
 * Расчёт основных параметров МНЛЗ по формулам (8.1)–(8.9) Токового.
 */
public class MnlzCalculator {

    /** Плотность литой стали, кг/м³. */
    public static final double STEEL_DENSITY = 7300.0;

    public MnlzResult calculate(MnlzInput input) {
        validate(input);

        double Mkg = input.getConverterMassT() * 1000.0;
        double B = input.getWidthM();
        double b = input.getThicknessM();
        double v = input.getCastingSpeedMPerMin();
        double tau = input.getCastingTimeMin();
        double kz = input.getSolidificationCoeff();

        // (8.2) число ручьев; при n≈1.05 в типовой задаче принимают 1 ручей
        double nRaw = Mkg / (B * b * STEEL_DENSITY * v * tau);
        int n = Math.max(1, (int) Math.round(nRaw));

        // (8.5) глубина лунки жидкой фазы
        double Ll = (kz * b * Mkg) / (0.9 * n * B * tau * STEEL_DENSITY);

        // (8.3) массовая скорость выхода заготовки, т/мин
        double Gz = B * b * STEEL_DENSITY * v / 1000.0;

        // (8.7) радиус и высота радиальной МНЛЗ
        double R = 2.0 * Ll / Math.PI;
        // (8.9) практический минимум радиуса
        double RminPractice = 42.0 * b;

        MnlzResult result = new MnlzResult();
        result.setStrands(n);
        result.setLiquidPoolDepthM(round2(Ll));
        result.setRadiusM(round2(Math.max(R, RminPractice)));
        result.setHeightM(round2(Math.max(R, RminPractice)));
        result.setMassRateTPerMin(round3(Gz));
        result.setMinRadiusPracticeM(round2(RminPractice));
        result.setMachineType(recommendType(Ll));
        return result;
    }

    public String recommendType(double liquidPoolM) {
        if (liquidPoolM <= 16) {
            return "Вертикальная / радиальная (выпрямление полностью затвердевшей заготовки)";
        }
        if (liquidPoolM <= 19) {
            return "Радиальная при выпрямлении полностью затвердевшей заготовки";
        }
        if (liquidPoolM <= 30) {
            return "Радиальная при выпрямлении заготовки в двухфазном состоянии";
        }
        return "Криволинейная с дифференцированным выпрямлением заготовки";
    }

    /**
     * Коэффициент полного затвердевания kз по типу заготовки.
     */
    public static double defaultSolidificationCoeff(double widthM, double thicknessM) {
        boolean billetLike = Math.abs(widthM - thicknessM) < 1e-6;
        if (billetLike) {
            return 240.0;
        }
        if (widthM > 1.2) {
            return 340.0;
        }
        return 290.0;
    }

    private void validate(MnlzInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Нет входных данных МНЛЗ");
        }
        if (input.getConverterMassT() <= 0 || input.getWidthM() <= 0 || input.getThicknessM() <= 0
                || input.getCastingSpeedMPerMin() <= 0 || input.getCastingTimeMin() <= 0
                || input.getSolidificationCoeff() <= 0) {
            throw new IllegalArgumentException("Все параметры МНЛЗ должны быть положительными");
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static double round3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
