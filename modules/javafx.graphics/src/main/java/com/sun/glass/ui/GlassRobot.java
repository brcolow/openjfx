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

public class GlassRobot {

    @Native public static final int MOUSE_LEFT_BTN   = 1;
    @Native public static final int MOUSE_RIGHT_BTN  = 2;
    @Native public static final int MOUSE_MIDDLE_BTN = 4;
    public static final int BYTE_BUFFER_BYTES_PER_COMPONENT = 1;
    public static final int INT_BUFFER_BYTES_PER_COMPONENT = 4;

    public static int convertToRobotMouseButton(MouseButton button) {
        switch (button) {
            case PRIMARY: return MOUSE_LEFT_BTN;
            case SECONDARY: return MOUSE_RIGHT_BTN;
            case MIDDLE: return MOUSE_MIDDLE_BTN;
            default: throw new IllegalArgumentException("MouseButton: " + button + " not supported by Robot");
        }
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
        if (bytesPerComponent == INT_BUFFER_BYTES_PER_COMPONENT) {
            IntBuffer intBuffer = (IntBuffer) pixels.getPixels();
            writeIntBufferToImage(intBuffer, image);
        }
        else if (bytesPerComponent == BYTE_BUFFER_BYTES_PER_COMPONENT) {
            ByteBuffer byteBuffer = (ByteBuffer) pixels.getPixels();
            writeByteBufferToImage(byteBuffer, image);
        }

        return image;
    }

    public static void writeIntBufferToImage(IntBuffer intBuffer, WritableImage image) {
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

    public static void writeByteBufferToImage(ByteBuffer byteBuffer, WritableImage image) {
        throw new UnsupportedOperationException("Writing from byte buffer is not supported.");
    }

    public static int interp(int pixels[], int x, int y, int w, int h, int fractx1, int fracty1) {
        int fractx0 = 256 - fractx1;
        int fracty0 = 256 - fracty1;
        int i = y * w + x;
        int rgb00 = (x < 0 || y < 0 || x >= w || y >= h) ? 0 : pixels[i];
        if (fracty1 == 0) {
            // No interplation with pixels[y+1]
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
