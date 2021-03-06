/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

package test.robot.com.sun.glass.ui.monocle;

import com.sun.glass.ui.monocle.TestLogShim;
import com.sun.glass.ui.Application;
import javafx.application.Platform;
import javafx.geometry.VerticalDirection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * This is a generic test for Glass robot. It is in the monocle.input package
 * because it uses the same test infrastructure as the Monocle input tests.
 */
public class RobotTest {

    @Rule public TestName name = new TestName();

    @Before
    public void setUpScreen() throws Exception {
        TestLogShim.reset();
        TestLogShim.log(name.getMethodName());
        TestApplication.showFullScreenScene();
    }

    @Test
    public void clickTest() throws Exception {
        TestApplication.getStage().getScene().setOnMouseClicked(
                (e) -> TestLogShim.format("Clicked at %.0f, %.0f",
                                      e.getScreenX(), e.getScreenY()));
        Platform.runLater(() -> {
            Robot robot = Application.GetApplication().createRobot();
            robot.mouseMove(300, 400);
            robot.mousePress(MouseButton.PRIMARY);
            robot.mouseRelease(MouseButton.PRIMARY);
        });
        TestLogShim.waitForLog("Clicked at 300, 400");
    }

    @Test
    public void typeTest() throws Exception {
        TestApplication.getStage().getScene().setOnKeyTyped(
                (e) ->TestLogShim.format("Typed '%s'", e.getCharacter()));
        Platform.runLater(() -> {
            Robot robot = Application.GetApplication().createRobot();
            robot.keyPress(KeyCode.A);
            robot.keyRelease(KeyCode.A);
        });
        TestLogShim.waitForLog("Typed 'a'");
        Platform.runLater(() -> {
            Robot robot = Application.GetApplication().createRobot();
            robot.keyPress(KeyCode.SHIFT);
            robot.keyPress(KeyCode.B);
            robot.keyRelease(KeyCode.B);
            robot.keyRelease(KeyCode.SHIFT);
        });
        TestLogShim.waitForLog("Typed 'B'");
    }

    @Test
    public void scrollTest() throws Exception {
        TestApplication.getStage().getScene().setOnScroll(
                (e) -> TestLogShim.format("Scroll: %.0f at %.0f, %.0f",
                                      Math.signum(e.getDeltaY()),
                                      e.getScreenX(),
                                      e.getScreenY()));
        Platform.runLater(() -> {
            Robot robot = Application.GetApplication().createRobot();
            robot.mouseMove(300, 300);
            robot.mouseWheel(10, VerticalDirection.UP);
        });
        TestLogShim.waitForLog("Scroll: 1 at 300, 300");
        Platform.runLater(() -> {
            Robot robot = Application.GetApplication().createRobot();
            robot.mouseMove(310, 320);
            robot.mouseWheel(10, VerticalDirection.DOWN);
        });
        TestLogShim.waitForLog("Scroll: -1 at 310, 320");
    }

}
