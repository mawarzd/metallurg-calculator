package com.metallurg.calculator;

import com.metallurg.calculator.db.DatabaseSeeder;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.MnlzResult;
import com.metallurg.calculator.service.MnlzCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты расчёта МНЛЗ по типовой задаче §8.2. */
class MnlzCalculatorTest {

    private final MnlzCalculator calculator = new MnlzCalculator();

    @Test
    void typicalSlabTaskMatchesBookOrderOfMagnitude() {
        MnlzInput input = DatabaseSeeder.typicalMnlz();
        MnlzResult result = calculator.calculate(input);

        assertEquals(1, result.getStrands());
        assertEquals(13.2, result.getLiquidPoolDepthM(), 0.3);
        assertEquals(8.4, result.getRadiusM(), 0.5);
        assertTrue(result.getMachineType().toLowerCase().contains("радиал")
                || result.getMachineType().toLowerCase().contains("вертикал"));
    }

    @Test
    void solidificationCoeffDependsOnSection() {
        assertEquals(240.0, MnlzCalculator.defaultSolidificationCoeff(0.2, 0.2));
        assertEquals(340.0, MnlzCalculator.defaultSolidificationCoeff(1.3, 0.16));
        assertEquals(290.0, MnlzCalculator.defaultSolidificationCoeff(1.0, 0.2));
    }
}
