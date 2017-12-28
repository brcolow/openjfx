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
package com.sun.glass.ui;

import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import com.sun.javafx.image.PixelUtils;

public class GlassRobot {

    @Native public static final int MOUSE_LEFT_BTN    = 1 << 0;
    @Native public static final int MOUSE_RIGHT_BTN   = 1 << 1;
    @Native public static final int MOUSE_MIDDLE_BTN  = 1 << 2;
    @Native public static final int MOUSE_BACK_BTN    = 1 << 3;
    @Native public static final int MOUSE_FORWARD_BTN = 1 << 4;

    public static int convertToRobotMouseButton(MouseButton button) {
        switch (button) {
            case PRIMARY: return MOUSE_LEFT_BTN;
            case SECONDARY: return MOUSE_RIGHT_BTN;
            case MIDDLE: return MOUSE_MIDDLE_BTN;
            case BACK: return MOUSE_BACK_BTN;
            case FORWARD: return MOUSE_FORWARD_BTN;
            default: throw new IllegalArgumentException("MouseButton: " + button + " not supported by Robot");
        }
    }

    public static int convertToRobotMouseButton(MouseButton[] buttons) {
        int ret = 0;
        for (MouseButton button : buttons) {
            switch (button) {
                case PRIMARY: ret &= MOUSE_LEFT_BTN; break;
                case SECONDARY: ret &= MOUSE_RIGHT_BTN; break;
                case MIDDLE: ret &= MOUSE_MIDDLE_BTN; break;
                case BACK: ret &= MOUSE_BACK_BTN; break;
                case FORWARD: ret &= MOUSE_FORWARD_BTN; break;
                default: throw new IllegalArgumentException("MouseButton: " + button + " not supported by Robot");
            }
        }
        return ret;
    }

    public static Color convertFromIntArgb(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red   = (color >> 16) & 0xFF;
        int green = (color >>  8) & 0xFF;
        int blue  =  color        & 0xFF;
        return new Color(red / 255d, green / 255d, blue / 255d, alpha / 255d);
    }

    public static WritableImage convertFromPixels(Pixels pixels) {
        int width = pixels.getWidth();
        int height = pixels.getHeight();
        WritableImage image = new WritableImage(width, height);

        int bytesPerComponent = pixels.getBytesPerComponent();
        if (bytesPerComponent == 4) {
            IntBuffer intBuffer = (IntBuffer) pixels.getPixels();
            writeIntBufferToImage(intBuffer, image);
        }
        else if (bytesPerComponent == 1) {
            ByteBuffer byteBuffer = (ByteBuffer) pixels.getPixels();
            writeByteBufferToImage(byteBuffer, image);
        }

        return image;
    }

    private static void writeIntBufferToImage(IntBuffer intBuffer, WritableImage image) {
        PixelWriter pixelWriter = image.getPixelWriter();
        double width = image.getWidth();
        double height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = intBuffer.get();
                pixelWriter.setArgb(x, y, argb);
            }
        }
    }

    private static void writeByteBufferToImage(ByteBuffer byteBuffer, WritableImage image) {
        PixelWriter pixelWriter = image.getPixelWriter();
        double width = image.getWidth();
        double height = image.getHeight();

        int format = Pixels.getNativeFormat();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (format == Pixels.Format.BYTE_BGRA_PRE) {
                    pixelWriter.setArgb(x, y, PixelUtils.PretoNonPre(bgraPreToRgbaPre(byteBuffer.getInt())));
                } else if (format == Pixels.Format.BYTE_ARGB) {
                    pixelWriter.setArgb(x, y, byteBuffer.getInt());
                }
            }
        }
    }

    private static int bgraPreToRgbaPre(int bgraPre) {
        return (bgraPre & 0xff) | ((bgraPre & 0xff) << 8) |
                ((bgraPre & 0xff) << 16) | (bgraPre << 24);
    }

    public static int interp(int pixels[], int x, int y, int w, int h, int fractx1, int fracty1) {
        int fractx0 = 256 - fractx1;
        int fracty0 = 256 - fracty1;
        int i = y * w + x;
        int rgb00 = (x < 0 || y < 0 || x >= w || y >= h) ? 0 : pixels[i];
        if (fracty1 == 0) {
            // No interpolation with pixels[y+1]
            if (fractx1 == 0) {
                // No interpolation with any neighbors
                return rgb00;
            }
            int rgb10 = (y < 0 || x+1 >= w || y >= h) ? 0 : pixels[i+1];
            return interp(rgb00, rgb10, fractx0, fractx1);
        } else if (fractx1 == 0) {
            // No interpolation with pixels[x+1]
            int rgb01 = (x < 0 || x >= w || y+1 >= h) ? 0 : pixels[i+w];
            return interp(rgb00, rgb01, fracty0, fracty1);
        } else {
            // All 4 neighbors must be interpolated
            int rgb10 = (y < 0 || x+1 >= w || y >= h) ? 0 : pixels[i+1];
            int rgb01 = (x < 0 || x >= w || y+1 >= h) ? 0 : pixels[i+w];
            int rgb11 = (x+1 >= w || y+1 >= h) ? 0 : pixels[i+w+1];
            return interp(interp(rgb00, rgb10, fractx0, fractx1),
                    interp(rgb01, rgb11, fractx0, fractx1),
                    fracty0, fracty1);
        }
    }

    public static int interp(int rgb0, int rgb1, int fract0, int fract1) {
        int a0 = (rgb0 >> 24) & 0xff;
        int r0 = (rgb0 >> 16) & 0xff;
        int g0 = (rgb0 >>  8) & 0xff;
        int b0 = (rgb0      ) & 0xff;
        int a1 = (rgb1 >> 24) & 0xff;
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >>  8) & 0xff;
        int b1 = (rgb1      ) & 0xff;
        int a = (a0 * fract0 + a1 * fract1) >> 8;
        int r = (r0 * fract0 + r1 * fract1) >> 8;
        int g = (g0 * fract0 + g1 * fract1) >> 8;
        int b = (b0 * fract0 + b1 * fract1) >> 8;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

}
