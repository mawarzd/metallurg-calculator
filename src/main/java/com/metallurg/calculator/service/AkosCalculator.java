package com.metallurg.calculator.service;

import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.model.Ferroalloy;
import com.metallurg.calculator.model.SteelComposition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Расчёт раскисления и легирования стали на АКОС по формуле (7.1) Токового.
 * Mфер = Mж.ст · ([C]гот − [C]исх) · 100 / ([C]фер · (100 − Куг))
 */
public class AkosCalculator {

    private static final Map<String, Ferroalloy> DEFAULT_ALLOYS = defaultAlloys();

    public AkosResult calculate(AkosInput input) {
        if (input == null || input.getBefore() == null || input.getTarget() == null) {
            throw new IllegalArgumentException("Не задан состав стали");
        }
        if (input.getMeltMassKg() <= 0) {
            throw new IllegalArgumentException("Масса плавки должна быть больше нуля");
        }

        SteelComposition before = input.getBefore();
        SteelComposition target = input.getTarget();
        double mass = input.getMeltMassKg();

        AkosResult result = new AkosResult();

        // Порядок как в типовой задаче §7.2
        double mMn = ferroMass(mass, target.getMn(), before.getMn(), DEFAULT_ALLOYS.get("FMn78"));
        double mSi = ferroMass(mass, target.getSi(), before.getSi(), DEFAULT_ALLOYS.get("FS65"));
        double mCr = ferroMass(mass, target.getCr(), before.getCr(), DEFAULT_ALLOYS.get("FCr100A"));
        double mMo = ferroMass(mass, target.getMo(), before.getMo(), DEFAULT_ALLOYS.get("FMo52"));
        double mNi = ferroMass(mass, target.getNi(), before.getNi(), DEFAULT_ALLOYS.get("Ni"));
        double mAl = ferroMass(mass, target.getAl(), before.getAl(), DEFAULT_ALLOYS.get("Al"));

        result.getFerroalloyMassKg().put("Ферромарганец ФМн78", round1(mMn));
        result.getFerroalloyMassKg().put("Ферросилиций ФС65", round1(mSi));
        result.getFerroalloyMassKg().put("Феррохром ФХ100А", round1(mCr));
        result.getFerroalloyMassKg().put("Ферромолибден ФМо52", round1(mMo));
        result.getFerroalloyMassKg().put("Никель технический", round1(mNi));
        result.getFerroalloyMassKg().put("Алюминий первичный", round1(mAl));

        // Упрощённый баланс массы и итогового состава (с учётом угара целевых элементов)
        ElementMasses em = new ElementMasses();
        em.addFromSteel(mass, before);
        em.addAlloy(mMn, DEFAULT_ALLOYS.get("FMn78"));
        em.addAlloy(mSi, DEFAULT_ALLOYS.get("FS65"));
        em.addAlloy(mCr, DEFAULT_ALLOYS.get("FCr100A"));
        em.addAlloy(mMo, DEFAULT_ALLOYS.get("FMo52"));
        em.addAlloy(mNi, DEFAULT_ALLOYS.get("Ni"));
        em.addAlloy(mAl, DEFAULT_ALLOYS.get("Al"));

        double totalMass = em.total();
        SteelComposition pct = em.toPercent(totalMass);

        // Науглероживание до целевого C, если после легирования C ниже цели
        Ferroalloy carb = DEFAULT_ALLOYS.get("Carb");
        double mC = 0;
        if (pct.getC() < target.getC() - 1e-6) {
            mC = ferroMass(totalMass, target.getC(), pct.getC(), carb);
            em.addAlloy(mC, carb);
            totalMass = em.total();
            pct = em.toPercent(totalMass);
        }

        result.setCarburizerMassKg(round1(mC));
        result.getFerroalloyMassKg().put("Науглероживатель", round1(mC));
        result.setFinalMassKg(round1(totalMass));
        result.setFinalCompositionPct(pct);
        return result;
    }

    /**
     * Формула (7.1). Если прирост элемента ≤ 0, расход материала = 0.
     */
    public double ferroMass(double meltMassKg, double targetPct, double initialPct, Ferroalloy alloy) {
        double delta = targetPct - initialPct;
        if (delta <= 0) {
            return 0;
        }
        double denom = alloy.getElementPct() * (100.0 - alloy.getBurnLossPct());
        if (denom <= 0) {
            throw new IllegalArgumentException("Некорректные параметры ферросплава " + alloy.getCode());
        }
        return meltMassKg * delta * 100.0 / denom;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static Map<String, Ferroalloy> defaultAlloys() {
        Map<String, Ferroalloy> map = new LinkedHashMap<>();
        // Табл. 7.1 + средние угары из §7.1
        map.put("FMn78", new Ferroalloy("FMn78", "Ферромарганец ФМн78", "Mn", 80,
                7, 80, 1.5, 0.03, 0.35, 0, 0, 0, 0, 10.6, 10));
        map.put("FS65", new Ferroalloy("FS65", "Ферросилиций ФС65", "Si", 65,
                0, 0.4, 65, 0.02, 0.05, 0.4, 0, 0, 2.5, 31.6, 15));
        map.put("FCr100A", new Ferroalloy("FCr100A", "Феррохром ФХ100А", "Cr", 68,
                0.9, 0, 0.8, 0.02, 0.02, 68, 0, 0, 0.2, 30, 10));
        map.put("FMo52", new Ferroalloy("FMo52", "Ферромолибден ФМо52", "Mo", 50,
                0.2, 1.0, 0.8, 0.02, 0.1, 0, 0, 50, 0, 46.4, 0));
        map.put("Ni", new Ferroalloy("Ni", "Никель технический", "Ni", 96.5,
                0, 0, 0, 0, 0, 2.0, 96.5, 0, 0, 1.5, 0));
        map.put("Al", new Ferroalloy("Al", "Алюминий первичный", "Al", 97,
                0, 0, 1.5, 0, 1.5, 0, 0, 0, 97, 0, 40));
        map.put("Carb", new Ferroalloy("Carb", "Науглероживатель", "C", 99.8,
                99.8, 0, 0.2, 0, 0, 0, 0, 0, 0, 0, 40));
        return map;
    }

    public static Map<String, Ferroalloy> alloys() {
        return DEFAULT_ALLOYS;
    }

    /** Накопитель масс элементов, кг. */
    private static final class ElementMasses {
        double c, mn, si, s, p, cr, ni, mo, al, fe;

        void addFromSteel(double massKg, SteelComposition pct) {
            c += massKg * pct.getC() / 100.0;
            mn += massKg * pct.getMn() / 100.0;
            si += massKg * pct.getSi() / 100.0;
            s += massKg * pct.getS() / 100.0;
            p += massKg * pct.getP() / 100.0;
            cr += massKg * pct.getCr() / 100.0;
            ni += massKg * pct.getNi() / 100.0;
            mo += massKg * pct.getMo() / 100.0;
            al += massKg * pct.getAl() / 100.0;
            double known = pct.getC() + pct.getMn() + pct.getSi() + pct.getS() + pct.getP()
                    + pct.getCr() + pct.getNi() + pct.getMo() + pct.getAl();
            fe += massKg * Math.max(0, 100.0 - known) / 100.0;
        }

        void addAlloy(double alloyMassKg, Ferroalloy alloy) {
            if (alloyMassKg <= 0) {
                return;
            }
            double retention = (100.0 - alloy.getBurnLossPct()) / 100.0;
            // Для целевого элемента учитываем угар; попутные — как в типовом примере книги
            c += alloyMassKg * alloy.getCPct() / 100.0 * (alloy.getElement().equals("C") ? retention : 1.0);
            mn += alloyMassKg * alloy.getMnPct() / 100.0 * (alloy.getElement().equals("Mn") ? retention : 1.0);
            si += alloyMassKg * alloy.getSiPct() / 100.0 * (alloy.getElement().equals("Si") ? retention : 1.0);
            s += alloyMassKg * alloy.getSPct() / 100.0;
            p += alloyMassKg * alloy.getPPct() / 100.0;
            cr += alloyMassKg * alloy.getCrPct() / 100.0 * (alloy.getElement().equals("Cr") ? retention : 1.0);
            ni += alloyMassKg * alloy.getNiPct() / 100.0 * (alloy.getElement().equals("Ni") ? retention : 1.0);
            mo += alloyMassKg * alloy.getMoPct() / 100.0 * (alloy.getElement().equals("Mo") ? retention : 1.0);
            al += alloyMassKg * alloy.getAlPct() / 100.0 * (alloy.getElement().equals("Al") ? retention : 1.0);
            fe += alloyMassKg * alloy.getFePct() / 100.0;
        }

        double total() {
            return c + mn + si + s + p + cr + ni + mo + al + fe;
        }

        SteelComposition toPercent(double totalMass) {
            SteelComposition pct = new SteelComposition();
            pct.setC(roundPct(c, totalMass));
            pct.setMn(roundPct(mn, totalMass));
            pct.setSi(roundPct(si, totalMass));
            pct.setS(roundPct(s, totalMass));
            pct.setP(roundPct(p, totalMass));
            pct.setCr(roundPct(cr, totalMass));
            pct.setNi(roundPct(ni, totalMass));
            pct.setMo(roundPct(mo, totalMass));
            pct.setAl(roundPct(al, totalMass));
            return pct;
        }

        private static double roundPct(double kg, double total) {
            if (total <= 0) {
                return 0;
            }
            return Math.round(kg / total * 100.0 * 1000.0) / 1000.0;
        }
    }
}
