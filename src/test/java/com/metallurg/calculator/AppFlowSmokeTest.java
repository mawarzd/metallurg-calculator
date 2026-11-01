package com.metallurg.calculator;

import com.metallurg.calculator.dao.CalculationDao;
import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.db.DatabaseSeeder;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.model.MnlzResult;
import com.metallurg.calculator.service.AkosCalculator;
import com.metallurg.calculator.service.AuthService;
import com.metallurg.calculator.service.MnlzCalculator;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Сквозной smoke: БД → вход → FXML → расчёты по типовым задачам. */
class AppFlowSmokeTest {

    @BeforeAll
    static void setup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                com.sun.javafx.application.PlatformImpl.startup(() -> {
                });
            } finally {
                latch.countDown();
            }
        }, "fx-init").start();
        assertTrue(latch.await(20, TimeUnit.SECONDS));
        Database.getInstance().initialize();
        DatabaseSeeder.seedIfNeeded();
        Thread.sleep(300);
    }

    @Test
    void adminCanLoginAndMainUiLoads() throws Exception {
        AuthService auth = new AuthService();
        assertEquals("admin", auth.login("admin", "admin").getUsername());

        Parent login = load("/fxml/login.fxml");
        Parent main = load("/fxml/main.fxml");
        assertNotNull(login);
        assertNotNull(main);
    }

    @Test
    void typicalCalculationsPersist() throws Exception {
        AkosResult akos = new AkosCalculator().calculate(DatabaseSeeder.typicalAkos());
        MnlzResult mnlz = new MnlzCalculator().calculate(DatabaseSeeder.typicalMnlz());

        assertTrue(akos.getFerroalloyMassKg().get("Ферромарганец ФМн78") > 400);
        assertEquals(1, mnlz.getStrands());
        assertEquals(13.2, mnlz.getLiquidPoolDepthM(), 0.3);

        CalculationDao dao = new CalculationDao();
        int before = dao.countAllCalculations();
        var akosIn = DatabaseSeeder.typicalAkos();
        akosIn.setSourceMode("test");
        dao.saveAkos(null, akosIn, akos);
        var mnlzIn = DatabaseSeeder.typicalMnlz();
        mnlzIn.setSourceMode("test");
        dao.saveMnlz(null, mnlzIn, mnlz);
        assertTrue(dao.countAllCalculations() >= before + 2);
    }

    private static Parent load(String resource) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Parent> root = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                root.set(FXMLLoader.load(AppFlowSmokeTest.class.getResource(resource)));
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(30, TimeUnit.SECONDS), "timeout " + resource);
        if (error.get() != null) {
            fail(error.get());
        }
        return root.get();
    }
}
