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

package com.sun.glass.ui.ios;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.GlassRobot;

import java.nio.IntBuffer;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;

/**
 * iOS platform implementation class of test automation Robot.
 */
final class IosRobot extends Robot {

    private long ptr = 0;

    // init and create native robot object
    private native long _init();

    @Override public void create() {
        ptr = _init();
    }

    // release native robot object
    private native void _destroy(long ptr);
    @Override public void destroy() {
        _destroy(ptr);
        ptr = 0;
    }

    // synthesize key press
    private native void _keyPress(long ptr, int code);
    @Override public void keyPress(KeyCode code) {
        if (ptr == 0) {
            return;
        }
        _keyPress(ptr, code.getCode());
    }

    // synthesize key release
    private native void _keyRelease(long ptr, int code);
    @Override public void keyRelease(KeyCode code) {
        if (ptr == 0) {
            return;
        }
        _keyRelease(ptr, code.getCode());
    }

    // synthesize mouse motion
    private native void _mouseMove(long ptr, int x, int y);
    @Override public void mouseMove(int x, int y) {
        if (ptr == 0) {
            return;
        }
        _mouseMove(ptr, x, y);
    }

    // synthesize mouse press of buttons
    private native void _mousePress(long ptr, int buttons);
    @Override public void mousePress(MouseButton button) {
        if (ptr == 0) {
            return;
        }
        _mousePress(ptr, GlassRobot.convertToRobotMouseButton(button));
    }

    // synthesize mouse release of buttons
    private native void _mouseRelease(long ptr, int buttons);
    @Override public void mouseRelease(MouseButton button) {
        if (ptr == 0) {
            return;
        }
        _mouseRelease(ptr, GlassRobot.convertToRobotMouseButton(button));
    }

    private native void _mouseWheel(long ptr, int wheelAmt);
    @Override public void mouseWheel(int wheelAmt) {
        if (ptr == 0) {
            return;
        }
        _mouseWheel(ptr, wheelAmt);
    }

    // get x-coordinate of mouse location
    private native int _getMouseX(long ptr);
    @Override public int getMouseX() {
        if (ptr == 0) {
            return 0;
        }
        return _getMouseX(ptr);
    }

    // get x-coordinate of mouse location
    private native int _getMouseY(long ptr);
    @Override public int getMouseY() {
        if (ptr == 0) {
            return 0;
        }
        return _getMouseY(ptr);
    }

    private native int _getPixelColor(long ptr, int x, int y);
    @Override public Color getPixelColor(int x, int y) {
        if (ptr == 0) {
            return GlassRobot.convertFromIntArgb(0);
        }
        return GlassRobot.convertFromIntArgb(_getPixelColor(ptr, x, y));
    }

    @Override
    public void getScreenCapture(int x, int y, int width, int height, int[] data) {
        _getScreenCapture(ptr, x, y, width, height, data);
    }

    // capture bitmap image of (x, y, x + width, y + height) area
    native private void _getScreenCapture(long ptr, int x, int y, int width, int height, int[] data);

    @Override protected Image getScreenCapture(int x, int y, int width, int height, boolean isHiDPI) {
        if (ptr == 0) {
            return null;
        }
        int data[] = new int[width * height];
        _getScreenCapture(ptr, x, y, width, height, data);
        return GlassRobot.convertFromPixels(Application.GetApplication().createPixels(
                width, height, IntBuffer.wrap(data)));
    }
}

