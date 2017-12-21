/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.glass.ui.monocle;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.GlassRobot;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

class MonocleRobot extends Robot {
    @Override
    public void create() {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void keyPress(KeyCode code) {
        Platform.runLater(() -> {
            KeyState state = new KeyState();
            KeyInput.getInstance().getState(state);
            state.pressKey(code.getCode());
            KeyInput.getInstance().setState(state);
        });
    }

    @Override
    public void keyRelease(KeyCode code) {
        Platform.runLater(() -> {
            KeyState state = new KeyState();
            KeyInput.getInstance().getState(state);
            state.releaseKey(code.getCode());
            KeyInput.getInstance().setState(state);
        });
    }

    @Override
    public void mouseMove(int x, int y) {
        Platform.runLater(() -> {
            MouseState state = new MouseState();
            MouseInput.getInstance().getState(state);
            state.setX(x);
            state.setY(y);
            MouseInput.getInstance().setState(state, false);
        });
    }

    @Override
    public void mousePress(MouseButton button) {
        Platform.runLater(() -> {
            MouseState state = new MouseState();
            MouseInput.getInstance().getState(state);
            state.pressButton(GlassRobot.convertToRobotMouseButton(button));
            MouseInput.getInstance().setState(state, false);
        });
    }

    @Override
    public void mouseRelease(MouseButton button) {
        Platform.runLater(() -> {
            MouseState state = new MouseState();
            MouseInput.getInstance().getState(state);
            state.pressButton(GlassRobot.convertToRobotMouseButton(button));
            MouseInput.getInstance().setState(state, false);
        });
    }

    @Override
    public void mouseWheel(int wheelAmt) {
        Platform.runLater(() -> {
            MouseState state = new MouseState();
            MouseInput mouse = MouseInput.getInstance();
            mouse.getState(state);
            int direction = wheelAmt < 0
                            ? MouseState.WHEEL_DOWN
                            : MouseState.WHEEL_UP;
            for (int i = 0; i < Math.abs(wheelAmt); i++) {
                state.setWheel(direction);
                mouse.setState(state, false);
                state.setWheel(MouseState.WHEEL_NONE);
                mouse.setState(state, false);
            }
        });
    }

    @Override
    public int getMouseX() {
        Application.checkEventThread();
        MouseState state = new MouseState();
        MouseInput.getInstance().getState(state);
        return state.getX();
    }

    @Override
    public int getMouseY() {
        Application.checkEventThread();
        MouseState state = new MouseState();
        MouseInput.getInstance().getState(state);
        return state.getY();
    }

    @Override
    public Color getPixelColor(int x, int y) {
        NativeScreen screen = NativePlatformFactory.getNativePlatform().getScreen();
        final int byteDepth = screen.getDepth() >>> 3;
        final int bwidth = screen.getWidth();
        final int bheight = screen.getHeight();

        if (x < 0 || x > bwidth || y < 0 || y > bheight) {
            return GlassRobot.convertFromIntArgb(0);
        }

        synchronized (NativeScreen.framebufferSwapLock) {

            ByteBuffer buffer = screen.getScreenCapture();

            if (byteDepth == 2) {
                ShortBuffer shortbuf = buffer.asShortBuffer();

                int v = shortbuf.get((y * bwidth) + x);
                int red = (v & 0xF800) >> 11 << 3;
                int green = (v & 0x7E0) >> 5 << 2;
                int blue = (v & 0x1F) << 3;

                int p = (0xff000000
                        | (red << 16)
                        | (green << 8)
                        | blue);
                return GlassRobot.convertFromIntArgb(p);
            } else if (byteDepth >= 4) {
                IntBuffer intbuf = buffer.asIntBuffer();
                return GlassRobot.convertFromIntArgb(intbuf.get((y * bwidth) + x));
            } else {
                throw new RuntimeException("Unknown bit depth");
            }
        }
    }

    @Override
    public Image getScreenCapture(int x, int y, int width, int height,
                                  boolean isHiDPI) {
        NativeScreen screen = NativePlatformFactory.getNativePlatform().getScreen();
        final int scrWidth = screen.getWidth();
        final int scrHeight = screen.getHeight();

        synchronized (NativeScreen.framebufferSwapLock) {
            IntBuffer buffer = screen.getScreenCapture().asIntBuffer();

            if (x == 0 && y == 0 && width == scrWidth && height == scrHeight) {
                return GlassRobot.convertFromPixels(new MonoclePixels(width, height, buffer));
            }

            IntBuffer ret = IntBuffer.allocate(width * height);
            int rowStop = Math.min(y + height, scrHeight);
            int colStop = Math.min(x + width, scrWidth);
            for (int row = y; row < rowStop; row++) {
                for (int col = x; col < colStop; col++) {
                    ret.put(buffer.get(row * scrWidth + col));
                }
            }

            ret.rewind();
            return GlassRobot.convertFromPixels(new MonoclePixels(width, height, ret));
        }
    }
}
