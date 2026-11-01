package com.metallurg.calculator.model;

/** Химический состав стали, %. */
public class SteelComposition {

    private double c;
    private double mn;
    private double si;
    private double s;
    private double p;
    private double cr;
    private double ni;
    private double mo;
    private double al;

    public SteelComposition() {
    }

    public SteelComposition(double c, double mn, double si, double s, double p,
                            double cr, double ni, double mo, double al) {
        this.c = c;
        this.mn = mn;
        this.si = si;
        this.s = s;
        this.p = p;
        this.cr = cr;
        this.ni = ni;
        this.mo = mo;
        this.al = al;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getMn() {
        return mn;
    }

    public void setMn(double mn) {
        this.mn = mn;
    }

    public double getSi() {
        return si;
    }

    public void setSi(double si) {
        this.si = si;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getCr() {
        return cr;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public double getNi() {
        return ni;
    }

    public void setNi(double ni) {
        this.ni = ni;
    }

    public double getMo() {
        return mo;
    }

    public void setMo(double mo) {
        this.mo = mo;
    }

    public double getAl() {
        return al;
    }

    public void setAl(double al) {
        this.al = al;
    }
}
