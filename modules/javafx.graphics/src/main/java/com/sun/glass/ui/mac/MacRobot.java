/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.glass.ui.mac;

import com.sun.glass.ui.GlassRobot;
import com.sun.glass.ui.Pixels;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;

/**
 * MacOSX platform implementation class for Robot.
 */
final class MacRobot extends Robot {

    // TODO: get rid of native Robot object
    private long ptr;

    native private long _init();
    @Override protected void create() {
        ptr = _init();
    }

    native protected void _destroy(long ptr);
    @Override protected void destroy() {
        if (ptr == 0) {
            return;
        }
        _destroy(ptr);
    }

    native protected void _keyPress(int code);

    @Override public void keyPress(KeyCode code) {
        _keyPress(code.getCode());
    }

    native protected void _keyRelease(int code);

    @Override public void keyRelease(KeyCode code) {
        _keyRelease(code.getCode());
    }

    native protected void _mouseMove(long ptr, int x, int y);
    @Override public void mouseMove(int x, int y) {
        if (ptr == 0) {
            return;
        }
        _mouseMove(ptr, x, y);
    }

    native protected void _mousePress(long ptr, int buttons);
    @Override public void mousePress(MouseButton button) {
        if (ptr == 0) {
            return;
        }
        _mousePress(ptr, GlassRobot.convertToRobotMouseButton(button));
    }

    native protected void _mouseRelease(long ptr, int buttons);
    @Override public void mouseRelease(MouseButton button) {
        if (ptr == 0) {
            return;
        }
        _mouseRelease(ptr, GlassRobot.convertToRobotMouseButton(button));
    }

    @Override native public void mouseWheel(int wheelAmt);

    native protected int _getMouseX(long ptr);
    @Override public int getMouseX() {
        if (ptr == 0) {
            return 0;
        }
        return _getMouseX(ptr);
    }

    native protected int _getMouseY(long ptr);
    @Override public int getMouseY() {
        if (ptr == 0) {
            return 0;
        }
        return _getMouseY(ptr);
    }

    native protected int _getPixelColor(int x, int y);
    @Override public Color getPixelColor(int x, int y) {
        return GlassRobot.convertFromIntArgb(_getPixelColor(x, y));
    }

    native protected Pixels _getScreenCapture(int x, int y, int width, int height, boolean isHiDPI);

    @Override protected Image getScreenCapture(int x, int y, int width, int height, boolean isHiDPI) {
        if (ptr == 0) {
            return null;
        }
        return GlassRobot.convertFromPixels(_getScreenCapture(x, y, width, height, isHiDPI));
    }
}

