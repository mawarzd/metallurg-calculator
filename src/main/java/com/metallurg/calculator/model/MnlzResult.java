package com.metallurg.calculator.model;

/** Результат расчёта основных параметров МНЛЗ. */
public class MnlzResult {

    private int strands;
    private double liquidPoolDepthM;
    private double radiusM;
    private double heightM;
    private double massRateTPerMin;
    private String machineType;
    private double minRadiusPracticeM;

    public int getStrands() {
        return strands;
    }

    public void setStrands(int strands) {
        this.strands = strands;
    }

    public double getLiquidPoolDepthM() {
        return liquidPoolDepthM;
    }

    public void setLiquidPoolDepthM(double liquidPoolDepthM) {
        this.liquidPoolDepthM = liquidPoolDepthM;
    }

    public double getRadiusM() {
        return radiusM;
    }

    public void setRadiusM(double radiusM) {
        this.radiusM = radiusM;
    }

    public double getHeightM() {
        return heightM;
    }

    public void setHeightM(double heightM) {
        this.heightM = heightM;
    }

    public double getMassRateTPerMin() {
        return massRateTPerMin;
    }

    public void setMassRateTPerMin(double massRateTPerMin) {
        this.massRateTPerMin = massRateTPerMin;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public double getMinRadiusPracticeM() {
        return minRadiusPracticeM;
    }

    public void setMinRadiusPracticeM(double minRadiusPracticeM) {
        this.minRadiusPracticeM = minRadiusPracticeM;
    }
}
