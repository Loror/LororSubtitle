package com.loror.subtitle.util;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.Locale;

public class SubColorUtil {

    /**
     * bgr颜色值转rbg
     */
    public static String bgr2rgb(String color) {
        if (TextUtils.isEmpty(color)) {
            return null;
        }
        if (color.length() == 8) {
            String alpha = color.substring(0, 2);
            try {
                int a = 0xFF - Integer.parseInt(alpha, 16);
                alpha = Integer.toString(a, 16);
                if (alpha.length() == 1) {
                    alpha = "0" + alpha;
                }
                return alpha + color.substring(6, 8)
                        + color.substring(4, 6)
                        + color.substring(2, 4);
            } catch (Exception e) {
                return "FF" + color.substring(6, 8)
                        + color.substring(4, 6)
                        + color.substring(2, 4);
            }
        }
        return "FF" + color.substring(4, 6)
                + color.substring(2, 4)
                + color.substring(0, 2);
    }

    /**
     * bgr颜色值转rbg颜色值
     */
    public static int parseBgr(String color) {
        if (TextUtils.isEmpty(color)) {
            throw new IllegalArgumentException("color不能为空");
        }
        color = bgr2rgb(color);
        return (int) Long.parseLong(color, 16);
    }

    /**
     * 解析argb
     */
    public static int parseArgb(String color) {
        if (TextUtils.isEmpty(color)) {
            throw new IllegalArgumentException("color不能为空");
        }
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        String a = "FF";
        String r;
        String g;
        String b;
        if (color.length() == 3) {
            r = color.substring(0, 1);
            g = color.substring(1, 2);
            b = color.substring(2, 3);
            r += r;
            g += g;
            b += b;
        } else if (color.length() == 4) {
            a = color.substring(0, 1);
            r = color.substring(1, 2);
            g = color.substring(2, 3);
            b = color.substring(3, 4);
            a += a;
            r += r;
            g += g;
            b += b;
        } else if (color.length() == 6) {
            r = color.substring(0, 2);
            g = color.substring(2, 4);
            b = color.substring(4, 6);
        } else if (color.length() > 6) {
            if (color.length() > 8) {
                color = color.substring(color.length() - 8);
            }
            a = color.substring(0, color.length() - 6);
            String rgb = color.substring(color.length() - 6);
            if (a.length() == 1) {
                a += a;
            }
            r = rgb.substring(0, 2);
            g = rgb.substring(2, 4);
            b = rgb.substring(4, 6);
        } else {
            r = null;
            g = null;
            b = null;
        }
        if (r == null || g == null || b == null) {
            throw new IllegalArgumentException("color颜色值错误：" + color + " r:" + r + " g:" + g + " b:" + b);
        }
        return Color.parseColor("#" + a.toUpperCase(Locale.getDefault())
                + r.toUpperCase(Locale.getDefault())
                + g.toUpperCase(Locale.getDefault())
                + b.toUpperCase(Locale.getDefault()));
    }

    /**
     * 修改色彩中alpha值
     */
    public static int replaceColorAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        alpha = (int) (Color.alpha(color) * (alpha / 255f));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 修改色彩中alpha值
     */
    public static int replaceColorAlpha(int color, float alphaRange) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = (int) (Color.alpha(color) * alphaRange);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
