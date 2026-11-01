package com.metallurg.calculator.model;

/** Входные данные расчёта параметров МНЛЗ. */
public class MnlzInput {

    private String steelGrade;
    private double converterMassT;
    private double widthM;
    private double thicknessM;
    private double castingSpeedMPerMin;
    private double castingTimeMin;
    private double solidificationCoeff;
    private String sourceMode;

    public String getSteelGrade() {
        return steelGrade;
    }

    public void setSteelGrade(String steelGrade) {
        this.steelGrade = steelGrade;
    }

    public double getConverterMassT() {
        return converterMassT;
    }

    public void setConverterMassT(double converterMassT) {
        this.converterMassT = converterMassT;
    }

    public double getWidthM() {
        return widthM;
    }

    public void setWidthM(double widthM) {
        this.widthM = widthM;
    }

    public double getThicknessM() {
        return thicknessM;
    }

    public void setThicknessM(double thicknessM) {
        this.thicknessM = thicknessM;
    }

    public double getCastingSpeedMPerMin() {
        return castingSpeedMPerMin;
    }

    public void setCastingSpeedMPerMin(double castingSpeedMPerMin) {
        this.castingSpeedMPerMin = castingSpeedMPerMin;
    }

    public double getCastingTimeMin() {
        return castingTimeMin;
    }

    public void setCastingTimeMin(double castingTimeMin) {
        this.castingTimeMin = castingTimeMin;
    }

    public double getSolidificationCoeff() {
        return solidificationCoeff;
    }

    public void setSolidificationCoeff(double solidificationCoeff) {
        this.solidificationCoeff = solidificationCoeff;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }
}
