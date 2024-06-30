package com.android.simplemusic.utils;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.android.simplemusic.R;

public class ColorUtils {
    static class RGBColor{
        public int r;
        public int g;
        public int b;
    }

    public static int analyzeColor(@NonNull Context context, String color) {
        switch (color) {
            case "red":
                return context.getColor(R.color.red);
            case "titian_red":
                return context.getColor(R.color.titian_red);
            case "bordeaux_red":
                return context.getColor(R.color.bordeaux_red);
            case "green":
                return context.getColor(R.color.green);
            case "marrs_green":
                return context.getColor(R.color.marrs_green);
            case "klein_blue":
                return context.getColor(R.color.klein_blue);
            case "sennelier_yellow":
                return context.getColor(R.color.sennelier_yellow);
            case "lavender_purple":
                return context.getColor(R.color.lavender_purple);
            default:
                return context.getColor(R.color.white);
        }
    }

    @ColorInt
    public static int getInverseColor(int r, int g, int b) {
        if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            return r * 256 * 256 + g * 256 + b;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @ColorInt
    public static int getInverseColor(@ColorInt int color) {
        String str_color = Integer.toHexString(color);
        if (str_color.length() == 8) {
            String str_r = str_color.substring(2, 4);
            String str_g = str_color.substring(4, 6);
            String str_b = str_color.substring(6, 8);
            int r = Integer.parseInt(str_r, 16);
            int g = Integer.parseInt(str_g, 16);
            int b = Integer.parseInt(str_b, 16);
            return getInverseColor(r, g, b);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
