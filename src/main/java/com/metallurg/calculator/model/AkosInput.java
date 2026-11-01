package com.metallurg.calculator.model;

/** Входные данные расчёта раскисления и легирования на АКОС. */
public class AkosInput {

    private String steelGrade;
    private double meltMassKg;
    private SteelComposition before;
    private SteelComposition target;
    private String sourceMode;

    public String getSteelGrade() {
        return steelGrade;
    }

    public void setSteelGrade(String steelGrade) {
        this.steelGrade = steelGrade;
    }

    public double getMeltMassKg() {
        return meltMassKg;
    }

    public void setMeltMassKg(double meltMassKg) {
        this.meltMassKg = meltMassKg;
    }

    public SteelComposition getBefore() {
        return before;
    }

    public void setBefore(SteelComposition before) {
        this.before = before;
    }

    public SteelComposition getTarget() {
        return target;
    }

    public void setTarget(SteelComposition target) {
        this.target = target;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }
}
