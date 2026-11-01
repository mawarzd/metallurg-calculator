package com.metallurg.calculator.util;

import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.model.SteelComposition;

import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/** Упрощённая сериализация JSON без внешних библиотек. */
public final class SimpleJson {

    private SimpleJson() {
    }

    public static String akosInput(AkosInput in) {
        SteelComposition b = in.getBefore();
        SteelComposition t = in.getTarget();
        return "{"
                + kv("steelGrade", in.getSteelGrade()) + ","
                + kn("meltMassKg", in.getMeltMassKg()) + ","
                + kv("sourceMode", in.getSourceMode()) + ","
                + "\"before\":" + composition(b) + ","
                + "\"target\":" + composition(t)
                + "}";
    }

    public static String akosResult(AkosResult r) {
        StringJoiner alloys = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, Double> e : r.getFerroalloyMassKg().entrySet()) {
            alloys.add("\"" + escape(e.getKey()) + "\":" + num(e.getValue()));
        }
        return "{"
                + "\"ferroalloys\":" + alloys + ","
                + kn("carburizerMassKg", r.getCarburizerMassKg()) + ","
                + kn("finalMassKg", r.getFinalMassKg()) + ","
                + "\"finalComposition\":" + composition(r.getFinalCompositionPct())
                + "}";
    }

    private static String composition(SteelComposition c) {
        if (c == null) {
            return "null";
        }
        return "{"
                + kn("C", c.getC()) + ","
                + kn("Mn", c.getMn()) + ","
                + kn("Si", c.getSi()) + ","
                + kn("S", c.getS()) + ","
                + kn("P", c.getP()) + ","
                + kn("Cr", c.getCr()) + ","
                + kn("Ni", c.getNi()) + ","
                + kn("Mo", c.getMo()) + ","
                + kn("Al", c.getAl())
                + "}";
    }

    private static String kv(String k, String v) {
        return "\"" + k + "\":\"" + escape(v == null ? "" : v) + "\"";
    }

    private static String kn(String k, double v) {
        return "\"" + k + "\":" + num(v);
    }

    private static String num(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
