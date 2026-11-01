package com.metallurg.calculator;

import com.metallurg.calculator.db.DatabaseSeeder;
import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.service.AkosCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты расчёта АКОС по типовой задаче §7.2. */
class AkosCalculatorTest {

    private final AkosCalculator calculator = new AkosCalculator();

    @Test
    void ferroMassMatchesBookExampleForFerromanganese() {
        // 100000·(0.40−0.10)·100 / (80·(100−10)) = 416.7 кг
        double mass = calculator.ferroMass(100_000, 0.40, 0.10, AkosCalculator.alloys().get("FMn78"));
        assertEquals(416.7, mass, 0.1);
    }

    @Test
    void typicalTaskProducesPositiveAlloyMasses() {
        AkosInput input = DatabaseSeeder.typicalAkos();
        AkosResult result = calculator.calculate(input);

        assertTrue(result.getFerroalloyMassKg().get("Ферромарганец ФМн78") > 400);
        assertTrue(result.getFerroalloyMassKg().get("Ферросилиций ФС65") > 500);
        assertTrue(result.getFerroalloyMassKg().get("Феррохром ФХ100А") > 2400);
        assertTrue(result.getFinalMassKg() > input.getMeltMassKg());
        assertEquals(0.25, result.getFinalCompositionPct().getC(), 0.05);
    }
}
