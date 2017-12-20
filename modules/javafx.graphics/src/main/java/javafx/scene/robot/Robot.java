/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
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
package javafx.scene.robot;

import static com.sun.javafx.FXPermissions.CREATE_ROBOT_PERMISSION;

import java.nio.IntBuffer;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import com.sun.glass.ui.GlassRobot;
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Screen;

/**
 * A {@code Robot} is used for simulating user interaction such as
 * typing keys, using the mouse, and capturing portions of the screen.
 *
 * @since 11
 */
public abstract class Robot {

    /**
     * Initializes any state needed for this {@code Robot}.
     */
    public abstract void create();

    public Robot() {
        // Ensure proper permission
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CREATE_ROBOT_PERMISSION);
        }
        Application.checkEventThread();
        create();
    }

    public abstract void destroy();

    /**
     * Generates a keyboard key pressed event for the given {@link KeyCode}.
     *
     * @param keyCode the key to press
     */
    public abstract void keyPress(KeyCode keyCode);

    /**
     * Generates a keyboard key released event for the given {@link KeyCode}.
     *
     * @param keyCode the key to release
     */
    public abstract void keyRelease(KeyCode keyCode);

    /**
     * Returns the current mouse x position.
     *
     * @return the current mouse x position
     */
    public abstract int getMouseX();

    /**
     * Returns the current mouse y position.
     *
     * @return the current mouse y position
     */
    public abstract int getMouseY();

    /**
     * Returns the current mouse (x, y) coordinates as a {@link Point2D}.
     *
     * @return the current mouse (x,y) coordinates
     */
    public Point2D getMousePosition() {
        Application.checkEventThread();
        return new Point2D(getMouseX(), getMouseY());
    }

    /**
     * Generates a mouse moved event to the specified (x,y) screen
     * coordinates.
     *
     * @param x screen coordinate x to move the mouse to
     * @param y screen coordinate y to move the mouse to
     */
    public abstract void mouseMove(int x, int y);

    /**
     * Generates a mouse press event for the specified {@link MouseButton}.
     *
     * @param button the mouse button to press
     */
    public abstract void mousePress(MouseButton button);

    /**
     * Generates a mouse release event for the specified {@link MouseButton}.
     *
     * @param button the mouse button to release
     */
    public abstract void mouseRelease(MouseButton button);

    /**
     * Generates a mouse wheel event.
     *
     * @param wheelAmt amount the wheel has turned of wheel turning
     */
    public abstract void mouseWheel(int wheelAmt);

    /**
     * Returns the {@link Color} of the pixel at the specified screen coordinates.
     *
     * @param x the x coordinate to get the pixel color from
     * @param y the y coordinate to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public abstract Color getPixelColor(int x, int y);

    /**
     * Returns the {@link Color} of the pixel at the specified screen coordinates.
     *
     * @param location the (x,y) coordinates to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public Color getPixelColor(Point2D location) {
        Application.checkEventThread();
        return getPixelColor((int) location.getX(), (int) location.getY());
    }

    public abstract void getScreenCapture(int x, int y, int width, int height, int[] data);

    /**
     * Returns a capture of the specified rectangular area of the screen.
     * <p>
     * If the {@code isHiDPI} argument is {@literal true}, the returned
     * {@code Image} object dimensions may differ from the requested {@code width}
     * and {@code height} depending on how many physical pixels the area occupies
     * on the screen. E.g. in HiDPI mode on the Mac (aka Retina display) the pixels
     * are doubled, and thus a screen capture of an area of size (10x10) pixels
     * will result in an Image with dimensions (20x20). Calling code should use the
     * returned objects's getWidth() and getHeight() methods to determine the image
     * size.
     * <p>
     * If {@code isHiDPI} is {@literal false}, the returned {@code Image} is of
     * the requested size. Note that in this case the image may be scaled in
     * order to fit to the requested dimensions if running on a HiDPI display.
     */
    protected Image getScreenCapture(int x, int y, int width, int height, boolean isHiDPI) {
        Screen mainScreen = Screen.getMainScreen();
        float uiScaleX = mainScreen.getPlatformScaleX();
        float uiScaleY = mainScreen.getPlatformScaleY();
        int data[];
        int dw, dh;
        if (uiScaleX == 1.0f && uiScaleY == 1.0f) {
            data = new int[width * height];
            getScreenCapture(x, y, width, height, data);
            dw = width;
            dh = height;
        } else {
            int pminx = (int) Math.floor(x * uiScaleX);
            int pminy = (int) Math.floor(y * uiScaleY);
            int pmaxx = (int) Math.ceil((x + width) * uiScaleX);
            int pmaxy = (int) Math.ceil((y + height) * uiScaleY);
            int pwidth = pmaxx - pminx;
            int pheight = pmaxy - pminy;
            int tmpdata[] = new int[pwidth * pheight];
            getScreenCapture(pminx, pminy, pwidth, pheight, tmpdata);
            if (isHiDPI) {
                data = tmpdata;
                dw = pwidth;
                dh = pheight;
            } else {
                data = new int[width * height];
                int index = 0;
                for (int iy = 0; iy < height; iy++) {
                    float rely = ((y + iy + 0.5f) * uiScaleY) - (pminy + 0.5f);
                    int irely = (int) Math.floor(rely);
                    int fracty = (int) ((rely - irely) * 256);
                    for (int ix = 0; ix < width; ix++) {
                        float relx = ((x + ix + 0.5f) * uiScaleX) - (pminx + 0.5f);
                        int irelx = (int) Math.floor(relx);
                        int fractx = (int) ((relx - irelx) * 256);
                        data[index++] =
                                GlassRobot.interp(tmpdata, irelx, irely, pwidth, pheight, fractx, fracty);
                    }
                }
                dw = width;
                dh = height;
            }
        }
        return GlassRobot.convertFromPixels(Application.GetApplication().createPixels(dw, dh, IntBuffer.wrap(data)));
    }

    /**
     * Returns a capture of the specified area of the screen.
     * <p>
     * It is equivalent to calling getScreenCapture(x, y, width, height, false),
     * i.e. this method takes a "LowDPI" screen shot.
     *
     * @return the screen capture of the specified {@code region} as an {@link Image}
     */
    public final Image getScreenCapture(int x, int y, int width, int height) {
        return getScreenCapture(x, y, width, height, false);
    }

    /**
     * Returns a capture of the specified area of the screen.
     * <p>
     * It is equivalent to calling getScreenCapture(x, y, width, height, false),
     * i.e. this method takes a "LowDPI" screen shot.
     *
     * @return the screen capture of the specified {@code region} as an {@link Image}
     */
    public final Image getScreenCapture(Rectangle2D region) {
        return getScreenCapture((int) region.getMinX(), (int) region.getMinY(),
                (int) region.getWidth(), (int) region.getHeight(), false);
    }


}
