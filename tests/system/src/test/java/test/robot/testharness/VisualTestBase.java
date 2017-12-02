/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.robot.testharness;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import test.util.Util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static test.util.Util.TIMEOUT;

import javax.imageio.ImageIO;

/**
 * Common base class for testing snapshot.
 */
public abstract class VisualTestBase {
    @Rule
    public TestName name = new TestName();

    // Used to launch the application before running any test
    private static final CountDownLatch launchLatch = new CountDownLatch(1);
    // Singleton Application instance
    private static MyApp myApp;
    // Scene instances used for testing
    private List<Stage> stages = new ArrayList<>();

    // Glass Robot instance
    Robot robot;

    // Application class. An instance is created and initialized before running
    // the first test, and it lives through the execution of all tests.
    public static class MyApp extends Application {

        Stage stage;

        @Override
        public void init() {
            VisualTestBase.myApp = this;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            Platform.setImplicitExit(false);
            this.stage = primaryStage;
            assertTrue(Platform.isFxApplicationThread());
            assertNotNull(primaryStage);

            launchLatch.countDown();
        }
    }

    @BeforeClass
    public static void doSetupOnce() {
        // Start the Application
        new Thread(() -> Application.launch(MyApp.class, (String[]) null)).start();

        try {
            if (!launchLatch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new AssertionFailedError("Timeout waiting for Application to launch");
            }
        } catch (InterruptedException ex) {
            AssertionFailedError err = new AssertionFailedError("Unexpected exception");
            err.initCause(ex);
            throw err;
        }

        assertEquals(0, launchLatch.getCount());
    }

    @AfterClass
    public static void doTeardownOnce() {
        Platform.exit();
    }

    @Before
    public void doSetup() {
        runAndWait(() -> robot = com.sun.glass.ui.Application.GetApplication().createRobot());
    }

    @After
    public void doTeardown() {
        runAndWait(() -> {
            if (!stages.isEmpty()) {
                for (final Stage stage : stages) {
                    if (stage.isShowing()) {
                        stage.hide();
                    }
                }
                stages.clear();
            }
        });
    }

    protected void runAndWait(final Runnable r) {
        Util.runAndWait(r);
    }

    // This must be called on the FX app thread
    protected Stage getStage() {
        Stage stage = new Stage();
        // Undecorated stage to workaround RT-39904
        stage.initStyle(StageStyle.UNDECORATED);
        stages.add(stage);
        return stage;
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new AssertionFailedError("Unexpected exception: " + ex);
        }
    }

    // This must be called on the FX app thread
    protected Color getColor(Scene scene, int x, int y) {
        x += scene.getX() + scene.getWindow().getX();
        y += scene.getY() + scene.getWindow().getY();
        return getColor(x, y);
    }

    // This must be called on the FX app thread
    protected Color getColor(int x, int y) {
        return robot.getPixelColor(x, y);
    }

    private static String colorToString(Color c) {
        int r = (int)(c.getRed() * 255.0);
        int g = (int)(c.getGreen() * 255.0);
        int b = (int)(c.getBlue() * 255.0);
        int a = (int)(c.getOpacity() * 255.0);
        return "rgba(" + r + "," + g + "," + b + "," + a + ")";
    }

    private void saveScreenshot() {
        Image screenCapture = robot.getScreenCapture(0, 0, (int) Screen.getPrimary().getBounds().getWidth(),
                (int) Screen.getPrimary().getBounds().getHeight());
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(screenCapture, null);
        try {
            String travisBuildDir = System.getenv("TRAVIS_BUILD_DIR");
            if (travisBuildDir != null) {
                File screenshot = new File(travisBuildDir + "/images/" + name.getMethodName() + ".png");
                File parent = screenshot.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("could not create directory: " + parent);
                    }
                }
                System.out.println("Saving screenshot to: " + screenshot.getCanonicalPath());
                ImageIO.write(bufferedImage, "PNG", screenshot);
            }
        }
        catch (IOException e) {
            System.out.println("Error saving screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void assertColorEquals(Color expected, Color actual, double delta) {
        if (!testColorEquals(expected, actual, delta)) {
            Platform.runLater(this::saveScreenshot);
            throw new AssertionFailedError("expected:" + colorToString(expected)
                    + " but was:" + colorToString(actual));
        }
    }

    protected boolean testColorEquals(Color expected, Color actual, double delta) {
        double deltaRed = Math.abs(expected.getRed() - actual.getRed());
        double deltaGreen = Math.abs(expected.getGreen() - actual.getGreen());
        double deltaBlue = Math.abs(expected.getBlue() - actual.getBlue());
        double deltaOpacity = Math.abs(expected.getOpacity() - actual.getOpacity());
        return (deltaRed <= delta && deltaGreen <= delta && deltaBlue <= delta && deltaOpacity <= delta);
    }

    protected void assertColorDoesNotEqual(Color notExpected, Color actual, double delta) {
        double deltaRed = Math.abs(notExpected.getRed() - actual.getRed());
        double deltaGreen = Math.abs(notExpected.getGreen() - actual.getGreen());
        double deltaBlue = Math.abs(notExpected.getBlue() - actual.getBlue());
        double deltaOpacity = Math.abs(notExpected.getOpacity() - actual.getOpacity());
        if (deltaRed < delta && deltaGreen < delta && deltaBlue < delta && deltaOpacity < delta) {
            throw new AssertionFailedError("not expected:" + colorToString(notExpected)
                    + " but was:" + colorToString(actual));
        }
    }

    private AnimationTimer timer;

    private void frameWait(int n) {
        final CountDownLatch frameCounter = new CountDownLatch(n);
        Platform.runLater(() -> {
            timer = new AnimationTimer() {
                @Override public void handle(long l) {
                    frameCounter.countDown();
                }
            };
            timer.start();
        });

        try {
            frameCounter.await();
        } catch (InterruptedException ex) {
            throw new AssertionFailedError("Unexpected exception: " + ex);
        } finally {
            runAndWait(() -> {
                if (timer != null) {
                    timer.stop();
                }
            });
        }
    }

    // Waits until the fist frame is rendered after the stage has been shown
    protected void waitFirstFrame() {
        // This is a temporary workaround until RT-28683 is implemented
        frameWait(100);
    }

    // Waits until the frame containing the current state of the scene has
    // been rendered
    protected void waitNextFrame() {
        // This is a temporary workaround until RT-28683 is implemented
        // Need to wait for the current frame in process and then the next frame
        // However, we get many intermittent failures with 2 and a very few with
        // 3, so we will wait for 5 frames.
        frameWait(5);
    }

}
