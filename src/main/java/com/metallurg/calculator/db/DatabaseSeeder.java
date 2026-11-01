package com.metallurg.calculator.db;

import com.metallurg.calculator.dao.CalculationDao;
import com.metallurg.calculator.dao.EquipmentDao;
import com.metallurg.calculator.dao.UserDao;
import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.SteelComposition;
import com.metallurg.calculator.service.AkosCalculator;
import com.metallurg.calculator.service.MnlzCalculator;
import com.metallurg.calculator.util.PasswordHasher;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/** Начальное наполнение БД (≥100 записей). */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seedIfNeeded() throws SQLException {
        EquipmentDao equipmentDao = new EquipmentDao();
        CalculationDao calculationDao = new CalculationDao();
        UserDao userDao = new UserDao();

        if (userDao.findByUsername("admin").isEmpty()) {
            String salt = PasswordHasher.generateSalt();
            userDao.insert("admin", PasswordHasher.hash("admin", salt), salt);
        }

        seedFerroalloys();

        if (equipmentDao.count() == 0) {
            seedEquipment(equipmentDao);
        }

        if (calculationDao.countAllCalculations() < 100) {
            seedCalculations(calculationDao);
        }
    }

    private static void seedFerroalloys() throws SQLException {
        try (var st = Database.getInstance().getConnection().createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM ferroalloys")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }
        String sql = """
                INSERT INTO ferroalloys(code, name, element, element_pct, c_pct, mn_pct, si_pct, s_pct, p_pct,
                    cr_pct, ni_pct, mo_pct, al_pct, fe_pct, burn_loss_pct)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Object[][] rows = {
                {"FMn78", "Ферромарганец ФМн78", "Mn", 80.0, 7.0, 80.0, 1.5, 0.03, 0.35, 0.0, 0.0, 0.0, 0.0, 10.6, 10.0},
                {"FS65", "Ферросилиций ФС65", "Si", 65.0, 0.0, 0.4, 65.0, 0.02, 0.05, 0.4, 0.0, 0.0, 2.5, 31.6, 15.0},
                {"FCr100A", "Феррохром ФХ100А", "Cr", 68.0, 0.9, 0.0, 0.8, 0.02, 0.02, 68.0, 0.0, 0.0, 0.2, 30.0, 10.0},
                {"FMo52", "Ферромолибден ФМо52", "Mo", 50.0, 0.2, 1.0, 0.8, 0.02, 0.1, 0.0, 0.0, 50.0, 0.0, 46.4, 0.0},
                {"Ni", "Никель технический", "Ni", 96.5, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 96.5, 0.0, 0.0, 1.5, 0.0},
                {"Al", "Алюминий первичный", "Al", 97.0, 0.0, 0.0, 1.5, 0.0, 1.5, 0.0, 0.0, 0.0, 97.0, 0.0, 40.0},
                {"Carb", "Науглероживатель", "C", 99.8, 99.8, 0.0, 0.2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 40.0},
                {"SMn20", "Силикомарганец СМн20", "Mn", 65.0, 1.0, 65.0, 25.0, 0.03, 0.1, 0.0, 0.0, 0.0, 0.0, 8.8, 12.0}
        };
        try (PreparedStatement ps = Database.getInstance().getConnection().prepareStatement(sql)) {
            for (Object[] r : rows) {
                for (int i = 0; i < r.length; i++) {
                    if (r[i] instanceof String s) {
                        ps.setString(i + 1, s);
                    } else {
                        ps.setDouble(i + 1, (Double) r[i]);
                    }
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedEquipment(EquipmentDao dao) throws SQLException {
        List<String[]> items = List.of(
                new String[]{"АКОС-120", "АКОС", "120", "Агрегат комплексной обработки стали", "Уралмаш"},
                new String[]{"АКОС-160", "АКОС", "160", "Внепечная обработка, раскисление и легирование", "НКМЗ"},
                new String[]{"АКОС-300", "АКОС", "300", "Ковшевая печь с продувкой Ar", "SMS group"},
                new String[]{"МНЛЗ криволинейная №1", "МНЛЗ", "140", "Слябовая машина непрерывного литья", "Danieli"},
                new String[]{"МНЛЗ радиальная №2", "МНЛЗ", "130", "Радиальная МНЛЗ сортовых заготовок", "Констар"},
                new String[]{"МНЛЗ вертикальная", "МНЛЗ", "50", "Вертикальная МНЛЗ опытного цеха", "ВНИИметмаш"},
                new String[]{"Конвертер 140 т", "Конвертер", "140", "Кислородный конвертер", "Азовмаш"},
                new String[]{"Конвертер 300 т", "Конвертер", "300", "Кислородный конвертер большой садкой", "SMS"},
                new String[]{"Промковш слябовый", "Промковш", "25", "Промежуточный ковш МНЛЗ", "Vesuvius"},
                new String[]{"Кристаллизатор слябовый", "Кристаллизатор", "0", "Медный кристаллизатор 250×1500", "KME"}
        );
        String[] makers = {"Уралмаш", "НКМЗ", "SMS", "Danieli", "Primetals", "Констар", "Азовмаш"};
        String[] types = {"АКОС", "МНЛЗ", "Конвертер", "Промковш", "Кристаллизатор", "Ковш"};
        for (String[] row : items) {
            dao.insert(row[0], row[1], Double.parseDouble(row[2]), row[3], row[4]);
        }
        for (int i = 1; i <= 40; i++) {
            String type = types[i % types.length];
            dao.insert(type + "-" + (100 + i), type, 50.0 + i * 3.5,
                    "Типовое оборудование витрины #" + i, makers[i % makers.length]);
        }
    }

    private static void seedCalculations(CalculationDao dao) throws SQLException {
        AkosCalculator akosCalculator = new AkosCalculator();
        MnlzCalculator mnlzCalculator = new MnlzCalculator();
        Random rnd = new Random(42);

        String[] grades = {"25Х2Н4МА", "25ХГСА", "40ХГНМ", "Ст3сп", "35ГС", "70", "40Х", "20ХН3А"};
        // Эталонная задача §7.2
        AkosInput sample = typicalAkos();
        sample.setSourceMode("seed");
        dao.saveAkos(null, sample, akosCalculator.calculate(sample));

        for (int i = 0; i < 55; i++) {
            AkosInput in = new AkosInput();
            in.setSteelGrade(grades[i % grades.length]);
            in.setMeltMassKg(80_000 + rnd.nextInt(80_000));
            in.setSourceMode("seed");
            SteelComposition before = new SteelComposition(
                    0.10 + rnd.nextDouble() * 0.15,
                    0.05 + rnd.nextDouble() * 0.2,
                    0,
                    0.01 + rnd.nextDouble() * 0.01,
                    0.01 + rnd.nextDouble() * 0.01,
                    0.05 + rnd.nextDouble() * 0.2,
                    0.2 + rnd.nextDouble() * 1.5,
                    0.05 + rnd.nextDouble() * 0.1,
                    0
            );
            SteelComposition target = new SteelComposition(
                    before.getC() + 0.05 + rnd.nextDouble() * 0.15,
                    before.getMn() + 0.2 + rnd.nextDouble() * 0.6,
                    0.2 + rnd.nextDouble() * 0.8,
                    0.02,
                    0.02,
                    before.getCr() + 0.4 + rnd.nextDouble() * 1.0,
                    before.getNi() + 0.3 + rnd.nextDouble() * 2.0,
                    before.getMo() + 0.05 + rnd.nextDouble() * 0.2,
                    0.03 + rnd.nextDouble() * 0.02
            );
            in.setBefore(before);
            in.setTarget(target);
            dao.saveAkos(null, in, akosCalculator.calculate(in));
        }

        MnlzInput sampleM = typicalMnlz();
        sampleM.setSourceMode("seed");
        dao.saveMnlz(null, sampleM, mnlzCalculator.calculate(sampleM));

        for (int i = 0; i < 55; i++) {
            MnlzInput in = new MnlzInput();
            in.setSteelGrade(grades[i % grades.length]);
            in.setConverterMassT(100 + rnd.nextInt(120));
            boolean slab = i % 2 == 0;
            in.setWidthM(slab ? 1.1 + rnd.nextDouble() * 0.5 : 0.1 + rnd.nextDouble() * 0.2);
            in.setThicknessM(slab ? 0.15 + rnd.nextDouble() * 0.1 : in.getWidthM());
            in.setCastingSpeedMPerMin(slab ? 0.8 + rnd.nextDouble() * 0.6 : 1.5 + rnd.nextDouble() * 3.0);
            in.setCastingTimeMin(50 + rnd.nextInt(40));
            in.setSolidificationCoeff(MnlzCalculator.defaultSolidificationCoeff(in.getWidthM(), in.getThicknessM()));
            in.setSourceMode("seed");
            dao.saveMnlz(null, in, mnlzCalculator.calculate(in));
        }
    }

    public static AkosInput typicalAkos() {
        AkosInput in = new AkosInput();
        in.setSteelGrade("25Х2Н4МА");
        in.setMeltMassKg(100_000);
        in.setBefore(new SteelComposition(0.15, 0.10, 0, 0.015, 0.010, 0.05, 2.00, 0.08, 0));
        // В книге для Mo цель 0.30 (ГОСТ), в решении используют 0.35 для примера массы ФМо
        in.setTarget(new SteelComposition(0.25, 0.40, 0.28, 0.02, 0.02, 1.58, 4.30, 0.35, 0.03));
        return in;
    }

    public static MnlzInput typicalMnlz() {
        MnlzInput in = new MnlzInput();
        in.setSteelGrade("Ст3сп");
        in.setConverterMassT(140);
        in.setWidthM(1.3);
        in.setThicknessM(0.16);
        in.setCastingSpeedMPerMin(1.3);
        in.setCastingTimeMin(67.5);
        in.setSolidificationCoeff(340);
        return in;
    }
}
