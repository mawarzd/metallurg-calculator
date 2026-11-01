package com.metallurg.calculator.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Результат расчёта АКОС. */
public class AkosResult {

    private final Map<String, Double> ferroalloyMassKg = new LinkedHashMap<>();
    private SteelComposition finalCompositionPct;
    private double finalMassKg;
    private double carburizerMassKg;

    public Map<String, Double> getFerroalloyMassKg() {
        return ferroalloyMassKg;
    }

    public SteelComposition getFinalCompositionPct() {
        return finalCompositionPct;
    }

    public void setFinalCompositionPct(SteelComposition finalCompositionPct) {
        this.finalCompositionPct = finalCompositionPct;
    }

    public double getFinalMassKg() {
        return finalMassKg;
    }

    public void setFinalMassKg(double finalMassKg) {
        this.finalMassKg = finalMassKg;
    }

    public double getCarburizerMassKg() {
        return carburizerMassKg;
    }

    public void setCarburizerMassKg(double carburizerMassKg) {
        this.carburizerMassKg = carburizerMassKg;
    }
}
