package com.metallurg.calculator;

import com.metallurg.calculator.db.Database;
import com.metallurg.calculator.db.DatabaseSeeder;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Smoke: FXML логина и главного окна грузятся без ошибок. */
class UiLoadSmokeTest {

    @BeforeAll
    static void initToolkitAndDb() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        new Thread(() -> {
            try {
                com.sun.javafx.application.PlatformImpl.startup(() -> {
                });
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        }, "fx-init").start();
        assertTrue(latch.await(20, TimeUnit.SECONDS), "JavaFX toolkit не стартовал");
        if (error.get() != null) {
            fail(error.get());
        }
        Database.getInstance().initialize();
        DatabaseSeeder.seedIfNeeded();
        Thread.sleep(400);
    }

    @Test
    void loginFxmlLoads() throws Exception {
        assertNotNull(loadOnFxThread("/fxml/login.fxml"));
    }

    @Test
    void mainFxmlLoads() throws Exception {
        assertNotNull(loadOnFxThread("/fxml/main.fxml"));
    }

    private static Parent loadOnFxThread(String resource) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Parent> root = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                root.set(FXMLLoader.load(UiLoadSmokeTest.class.getResource(resource)));
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Таймаут загрузки " + resource);
        if (error.get() != null) {
            throw error.get();
        }
        return root.get();
    }
}
