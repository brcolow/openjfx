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
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.GlassRobot;
import com.sun.javafx.tk.Toolkit;

/**
 * A {@code Robot} is used for simulating user interaction such as
 * typing keys on the keyboard and using the mouse as well as capturing
 * graphical information without requiring a {@link javafx.scene.Scene}
 * instance.
 *
 * @since 11
 */
public abstract class Robot {

    private final GlassRobot peer;

    public Robot() {
        // Ensure we have proper permission for creating a robot.
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CREATE_ROBOT_PERMISSION);
        }

        peer = Toolkit.getToolkit().createRobot();
    }

    /**
     * Presses the specified {@link KeyCode} key.
     *
     * @param keyCode the key to press
     */
    public void keyPress(KeyCode keyCode) {
        peer.keyPress(keyCode);
    }

    /**
     * Releases the specified {@link KeyCode} key.
     *
     * @param keyCode the key to release
     */
    public void keyRelease(KeyCode keyCode) {
        peer.keyRelease(keyCode);
    }

    /**
     * Types the specified {@link KeyCode} key.
     * <p>
     * This is a convenience method that is equivalent to calling
     * {@link #keyPress(KeyCode)} followed by {@link #keyRelease(KeyCode)}.
     *
     * @param keyCode the key to type
     */
    public final void keyType(KeyCode keyCode) {
        keyPress(keyCode);
        keyRelease(keyCode);
    }

    /**
     * Returns the current mouse x-position.
     *
     * @return the current mouse x-position
     */
    public double getMouseX() {
        return peer.getMouseX();
    }

    /**
     * Returns the current mouse y-position.
     *
     * @return the current mouse y-position
     */
    public double getMouseY() {
        return peer.getMouseY();
    }

    /**
     * Returns the current mouse (x, y) coordinates as a {@link Point2D}.
     *
     * @return the current mouse (x,y) coordinates
     */
    public Point2D getMousePosition() {
        return new Point2D(getMouseX(), getMouseY());
    }

    /**
     * Moves the mouse to the specified (x,y) screen coordinates.
     *
     * @param x screen coordinate x to move the mouse to
     * @param y screen coordinate y to move the mouse to
     */
    public void mouseMove(double x, double y) {
        peer.mouseMove(x, y);
    }

    /**
     * Moves the mouse to the (x,y) screen coordinates specified by the
     * given {@code location}.
     *
     * @param location the (x,y) coordinates to move the mouse to
     */
    public final void mouseMove(Point2D location) {
        mouseMove(location.getX(), location.getY());
    }

    /**
     * Presses the specified {@link MouseButton}s.
     *
     * @param buttons the mouse buttons to press
     */
    public void mousePress(MouseButton... buttons) {
        peer.mousePress(buttons);
    }

    /**
     * Releases the specified {@link MouseButton}s.
     *
     * @param buttons the mouse buttons to release
     */
    public void mouseRelease(MouseButton... buttons) {
        peer.mouseRelease(buttons);
    }

    /**
     * Scrolls the mouse wheel by the specified amount. Positive {@code wheelAmt}s
     * scroll up whereas negative scroll down.
     *
     * @param wheelAmt the (signed) amount to scroll the wheel
     */
    protected void mouseWheel(int wheelAmt) {
        peer.mouseWheel(wheelAmt);
    }

    /**
     * Returns the {@link Color} of the pixel at the specified screen coordinates of the
     * primary screen.
     *
     * @param x the x coordinate to get the pixel color from
     * @param y the y coordinate to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public Color getPixelColor(double x, double y) {
        return peer.getPixelColor(x, y);
    }

    /**
     * Returns the {@link Color} of the pixel at the screen coordinates of the primary screen
     * specified by {@code location}.
     *
     * @param location the (x,y) coordinates to get the pixel color from
     * @return the pixel color at the specified screen coordinates
     */
    public final Color getPixelColor(Point2D location) {
        return getPixelColor(location.getX(), location.getY());
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
    public final WritableImage getScreenCapture(double x, double y, double width, double height, boolean scaleToFit) {
        return peer.getScreenCapture(x, y, width, height, scaleToFit);
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
    public final WritableImage getScreenCapture(double x, double y, double width, double height) {
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
    public final WritableImage getScreenCapture(Rectangle2D region) {
        return getScreenCapture(region.getMinX(), region.getMinY(),
                region.getWidth(), region.getHeight(), true);
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
    public final WritableImage getScreenCapture(Rectangle2D region, boolean scaleToFit) {
        return getScreenCapture(region.getMinX(), region.getMinY(),
                region.getWidth(), region.getHeight(), scaleToFit);
    }
}
