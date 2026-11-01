package com.metallurg.calculator.model;

/** Раскислитель / легирующий материал (табл. 7.1). */
public class Ferroalloy {

    private final String code;
    private final String name;
    private final String element;
    private final double elementPct;
    private final double cPct;
    private final double mnPct;
    private final double siPct;
    private final double sPct;
    private final double pPct;
    private final double crPct;
    private final double niPct;
    private final double moPct;
    private final double alPct;
    private final double fePct;
    private final double burnLossPct;

    public Ferroalloy(String code, String name, String element, double elementPct,
                      double cPct, double mnPct, double siPct, double sPct, double pPct,
                      double crPct, double niPct, double moPct, double alPct, double fePct,
                      double burnLossPct) {
        this.code = code;
        this.name = name;
        this.element = element;
        this.elementPct = elementPct;
        this.cPct = cPct;
        this.mnPct = mnPct;
        this.siPct = siPct;
        this.sPct = sPct;
        this.pPct = pPct;
        this.crPct = crPct;
        this.niPct = niPct;
        this.moPct = moPct;
        this.alPct = alPct;
        this.fePct = fePct;
        this.burnLossPct = burnLossPct;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getElement() {
        return element;
    }

    public double getElementPct() {
        return elementPct;
    }

    public double getCPct() {
        return cPct;
    }

    public double getMnPct() {
        return mnPct;
    }

    public double getSiPct() {
        return siPct;
    }

    public double getSPct() {
        return sPct;
    }

    public double getPPct() {
        return pPct;
    }

    public double getCrPct() {
        return crPct;
    }

    public double getNiPct() {
        return niPct;
    }

    public double getMoPct() {
        return moPct;
    }

    public double getAlPct() {
        return alPct;
    }

    public double getFePct() {
        return fePct;
    }

    public double getBurnLossPct() {
        return burnLossPct;
    }
}
