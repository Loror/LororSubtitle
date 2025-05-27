package com.loror.subtitle.render;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.loror.subtitle.util.TTFParser;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FontRender {

    public static final String TAG = "FontRender";

    public static boolean USE_FONT = true;

    private final static Map<String, Typeface> fonts = new HashMap<>();
    private final static Map<String, String> fontAlias = new HashMap<>();

    static {
        fontAlias.put("Microsoft YaHei", "微软雅黑");
        fontAlias.put("FZHei-B01", "方正黑体简体");
    }

    /**
     * 查找字体
     */
    public static synchronized Typeface findTypeface(String name) {
//        Logger.d(TAG, "findTypeface:" + name + " use:" + USE_FONT + " fonts:" + fonts.size());
        if (!USE_FONT) {
            return null;
        }
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (fonts.isEmpty()) {
            return null;
        }
        Typeface typeface = fonts.get(name);
        if (typeface != null) {
            return typeface;
        }
        String alias = fontAlias.get(name);
        if (alias != null) {
            typeface = fonts.get(alias);
            if (typeface != null) {
                fonts.put(alias, typeface);
                return typeface;
            }
        }
        for (Map.Entry<String, Typeface> item : fonts.entrySet()) {
            if (name.contains(item.getKey())) {
                return item.getValue();
            }
        }
        return null;
    }

    /**
     * 加载字体
     */
    public static void loadFonts(File dir) {
        Map<String, Typeface> loads = new HashMap<>();
        if (dir == null || dir.isFile()) {
            return;
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return;
            }
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            //Typeface支持ttc，但暂时无法获取ttc名称
            int type = getFontType(name);
            if (type > 0) {
                Typeface typeface = null;
                String fontName = "";
                String fileFontName = "";
                try {
                    fontName = name.substring(0, name.length() - 4);
                    typeface = Typeface.createFromFile(file);
                    if (type == 1) {
                        TTFParser ttfParser = new TTFParser();
                        ttfParser.parse(file.getAbsolutePath());
                        String fName = ttfParser.getFontName();
                        if (!TextUtils.isEmpty(fName)) {
                            fileFontName = fontName;
                            fontName = fName;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (typeface != null) {
                    loads.put(fontName, typeface);
                    if (!TextUtils.isEmpty(fileFontName)) {
                        loads.put(fileFontName, typeface);
                    }
                    Log.d(TAG, "loadFont-> name:" + fontName);
                }
            }
        }
        synchronized (SubtitlesRender.class) {
            fonts.clear();
            if (!loads.isEmpty()) {
                fonts.putAll(loads);
            }
        }
    }

    /**
     * 0 未知
     * 1 ttf
     * 2 otf
     * 3 ttc
     */
    private static int getFontType(String name) {
        String lowerName = name.toLowerCase(Locale.getDefault());
        if (lowerName.endsWith(".ttf")) {
            return 1;
        } else if (lowerName.endsWith(".otf")) {
            return 2;
        } else if (lowerName.endsWith(".ttc")) {
            return 2;
        }
        return 0;
    }
}
