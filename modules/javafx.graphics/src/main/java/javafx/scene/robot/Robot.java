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
import javafx.geometry.VerticalDirection;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.GlassRobot;

/**
 * A {@code Robot} is used for simulating user interaction such as
 * typing keys on the keyboard and using the mouse as well as capturing
 * graphical information without requiring a {@link javafx.scene.Scene}
 * instance.
 * <p>
 * A {@code Robot} instance can be obtained by calling
 * {@link javafx.application.Application#createRobot()}.
 *
 * @since 11
 */
public abstract class Robot {

    /**
     * Initializes any state necessary for this {@code Robot}. Called by
     * the {@code Robot} constructor.
     */
    protected abstract void create();

    protected Robot() {
        // Ensure we have proper permission for creating a robot.
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CREATE_ROBOT_PERMISSION);
        }

        Application.checkEventThread();
        create();
    }

    /**
     * Frees any resources allocated by this {@code Robot}.
     */
    protected abstract void destroy();

    /**
     * Presses the specified {@link KeyCode} key.
     *
     * @param keyCode the key to press
     */
    public abstract void keyPress(KeyCode keyCode);

    /**
     * Releases the specified {@link KeyCode} key.
     *
     * @param keyCode the key to release
     */
    public abstract void keyRelease(KeyCode keyCode);

    /**
     * Types the specified {@link KeyCode} key.
     * <p>
     * This is a convenience method that is equivalent to calling
     * {@link #keyPress(KeyCode)} followed by {@link #keyRelease(KeyCode)}.
     *
     * @param keyCode the key to type
     */
    public final void keyType(KeyCode keyCode) {
        Application.checkEventThread();
        keyPress(keyCode);
        keyRelease(keyCode);
    }

    /**
     * Returns the current mouse x-position.
     *
     * @return the current mouse x-position
     */
    public abstract int getMouseX();

    /**
     * Returns the current mouse y-position.
     *
     * @return the current mouse y-position
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
     * Moves the mouse to the specified (x,y) screen coordinates.
     *
     * @param x screen coordinate x to move the mouse to
     * @param y screen coordinate y to move the mouse to
     */
    public abstract void mouseMove(int x, int y);

    /**
     * Moves the mouse to the (x,y) screen coordinates specified by the
     * given {@code location}.
     *
     * @param location the (x,y) coordinates to move the mouse to
     */
    public final void mouseMove(Point2D location) {
        Application.checkEventThread();
        mouseMove((int) location.getX(), (int) location.getY());
    }

    /**
     * Presses the specified {@link MouseButton}.
     *
     * @param button the mouse button to press
     */
    public abstract void mousePress(MouseButton button);

    /**
     * Presses the specified {@link MouseButton}s at the same time.
     *
     * @param buttons the mouse buttons to press at the same time
     */
    public abstract void mousePress(MouseButton... buttons);

    /**
     * Releases the specified {@link MouseButton}.
     *
     * @param button the mouse button to release
     */
    public abstract void mouseRelease(MouseButton button);

    /**
     * Releases the specified {@link MouseButton}s at the same time.
     *
     * @param buttons the mouse buttons to release at the same time
     */
    public abstract void mouseRelease(MouseButton... buttons);

    /**
     * Scrolls the mouse wheel by the specified amount in the specified vertical
     * {@code direction}.
     *
     * @param wheelAmt the amount to scroll the wheel
     * @param direction the vertical direction, either up or down, to scroll in
     */
    public final void mouseWheel(int wheelAmt, VerticalDirection direction) {
        Application.checkEventThread();
        switch (direction) {
            case UP:
                mouseWheel(wheelAmt);
                break;
            case DOWN:
                mouseWheel(-wheelAmt);
                break;
            default: throw new IllegalArgumentException("unsupported direction: " + direction);
        }
    }

    /**
     * Scrolls the mouse wheel by the specified amount. Positive {@code wheelAmt}s
     * scroll up whereas negative scroll down.
     *
     * @param wheelAmt the (signed) amount to scroll the wheel
     */
    protected abstract void mouseWheel(int wheelAmt);

    /**
     * Returns the {@link Color} of the pixel at the specified screen coordinates.
     *
     * @param x the x coordinate to get the pixel color from
     * @param y the y coordinate to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public abstract Color getPixelColor(int x, int y);

    /**
     * Returns the {@link Color} of the pixel at the screen coordinates specified
     * by {@code location}.
     *
     * @param location the (x,y) coordinates to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public final Color getPixelColor(Point2D location) {
        Application.checkEventThread();
        return getPixelColor((int) location.getX(), (int) location.getY());
    }

    /**
     * Returns an {@code Image} containing the specified rectangular area of the screen.
     * <p>
     * If the {@code scaleToFit} argument is {@literal false}, the returned
     * {@code Image} object dimensions may differ from the requested {@code width}
     * and {@code height} depending on how many physical pixels the area occupies
     * on the screen. E.g. in HiDPI mode on the Mac (aka Retina display) the pixels
     * are doubled, and thus a screen capture of an area of size (10x10) pixels
     * will result in an {@code Image} with dimensions (20x20). Calling code should
     * use the returned images's {@link Image#getWidth() and {@link Image#getHeight()
     * methods to determine the actual image size.
     * <p>
     * If {@code scaleToFit} is {@literal true}, the returned {@code Image} is of
     * the requested size. Note that in this case the image will be scaled in
     * order to fit to the requested dimensions if necessary such as when running
     * on a HiDPI display.
     *
     * @param x the starting x-position of the rectangular area to capture
     * @param y the starting y-position of the rectangular area to capture
     * @param width the width of the rectangular area to capture
     * @param height the height of the rectangular area to capture
     * @param scaleToFit If {@literal true} the returned {@code Image} will be
     * scaled to fit the request dimensions, if necessary. Otherwise the size
     * of the returned image will depend on the output scale (DPI) of the primary
     * screen.
     */
    public final Image getScreenCapture(int x, int y, int width, int height, boolean scaleToFit) {
        Application.checkEventThread();
        Screen primaryScreen = Screen.getPrimary();
        double outputScaleX = primaryScreen.getOutputScaleX();
        double outputScaleY = primaryScreen.getOutputScaleY();
        int data[];
        int dw, dh;
        if (outputScaleX == 1.0f && outputScaleY == 1.0f) {
            // No scaling with be necessary regardless of if "scaleToFit" is set or not.
            data = new int[width * height];
            getScreenCapture(x, y, width, height, data);
            dw = width;
            dh = height;
        } else {
            // Compute the absolute pixel bounds that the requested size will fill given
            // the display's scale.
            int pminx = (int) Math.floor(x * outputScaleX);
            int pminy = (int) Math.floor(y * outputScaleY);
            int pmaxx = (int) Math.ceil((x + width) * outputScaleX);
            int pmaxy = (int) Math.ceil((y + height) * outputScaleY);
            int pwidth = pmaxx - pminx;
            int pheight = pmaxy - pminy;
            int tmpdata[] = new int[pwidth * pheight];
            getScreenCapture(pminx, pminy, pwidth, pheight, tmpdata);
            if (!scaleToFit) {
                data = tmpdata;
                dw = pwidth;
                dh = pheight;
            } else {
                // We must resize the image to fit the requested bounds. This means
                // resizing the pixel data array which we accomplish using bilinear (?)
                // interpolation.
                data = new int[width * height];
                int index = 0;
                for (int iy = 0; iy < height; iy++) {
                    double rely = ((y + iy + 0.5f) * outputScaleY) - (pminy + 0.5f);
                    int irely = (int) Math.floor(rely);
                    int fracty = (int) ((rely - irely) * 256);
                    for (int ix = 0; ix < width; ix++) {
                        double relx = ((x + ix + 0.5f) * outputScaleX) - (pminx + 0.5f);
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

        return GlassRobot.convertFromPixels(Application.GetApplication().createPixels(
                dw, dh, IntBuffer.wrap(data)));
    }

    /**
     * Returns an {@code Image} containing the specified rectangular area of the screen.
     * <p>
     * It is equivalent to calling {@code getScreenCapture(x, y, width, height, true)},
     * i.e. this method scales the image to fit the requested size.
     *
     * @param x the starting x-position of the rectangular area to capture
     * @param y the starting y-position of the rectangular area to capture
     * @param width the width of the rectangular area to capture
     * @param height the height of the rectangular area to capture
     * @return the screen capture of the specified {@code region} as an {@link Image}
     */
    public final Image getScreenCapture(int x, int y, int width, int height) {
        Application.checkEventThread();
        return getScreenCapture(x, y, width, height, true);
    }

    /**
     * Returns an {@code Image} containing the specified rectangular area of the screen.
     * <p>
     * It is equivalent to calling {@code getScreenCapture(x, y, width, height, true)},
     * i.e. this method scales the image to fit the requested size.
     *
     * @param region the rectangular area of the screen to capture
     * @return the screen capture of the specified {@code region} as an {@link Image}
     */
    public final Image getScreenCapture(Rectangle2D region) {
        Application.checkEventThread();
        return getScreenCapture((int) region.getMinX(), (int) region.getMinY(),
                (int) region.getWidth(), (int) region.getHeight(), true);
    }

    /**
     * Returns an {@code Image} containing the specified rectangular area of the screen.
     * <p>
     * If the {@code scaleToFit} argument is {@literal false}, the returned
     * {@code Image} object dimensions may differ from the requested {@code width}
     * and {@code height} depending on how many physical pixels the area occupies
     * on the screen. E.g. in HiDPI mode on the Mac (aka Retina display) the pixels
     * are doubled, and thus a screen capture of an area of size (10x10) pixels
     * will result in an {@code Image} with dimensions (20x20). Calling code should
     * use the returned images's {@link Image#getWidth() and {@link Image#getHeight()
     * methods to determine the actual image size.
     * <p>
     * If {@code scaleToFit} is {@literal true}, the returned {@code Image} is of
     * the requested size. Note that in this case the image will be scaled in
     * order to fit to the requested dimensions if necessary such as when running
     * on a HiDPI display.
     *
     * @param region the rectangular area of the screen to capture
     * @param scaleToFit If {@literal true} the returned {@code Image} will be
     * scaled to fit the request dimensions, if necessary. Otherwise the size
     * of the returned image will depend on the output scale (DPI) of the primary
     * screen.
     * @return the screen capture of the specified {@code region} as an {@link Image}
     */
    public final Image getScreenCapture(Rectangle2D region, boolean scaleToFit) {
        Application.checkEventThread();
        return getScreenCapture((int) region.getMinX(), (int) region.getMinY(),
                (int) region.getWidth(), (int) region.getHeight(), scaleToFit);
    }

    /**
     * Captures the specified rectangular area of the screen and uses it to fill the given
     * {@code data} array with the raw pixel data. The data is in RGBA format where each
     * pixel in the image is encoded as 4 bytes - one for each color component of each
     * pixel.
     *
     * @param x the starting x-position of the rectangular area to capture
     * @param y the starting y-position of the rectangular area to capture
     * @param width the width of the rectangular area to capture
     * @param height the height of the rectangular area to capture
     * @param data the array to fill with the raw pixel data corresponding to
     * the captured region
     */
    protected abstract void getScreenCapture(int x, int y, int width, int height, int[] data);
}
