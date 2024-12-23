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
            if (name.toLowerCase(Locale.getDefault()).endsWith(".ttf")) {
                Typeface typeface = null;
                String fontName = "";
                try {
                    fontName = name.substring(0, name.length() - 4);
                    typeface = Typeface.createFromFile(file);
                    TTFParser ttfParser = new TTFParser();
                    ttfParser.parse(file.getAbsolutePath());
                    String fName = ttfParser.getFontName();
                    if (!TextUtils.isEmpty(fName)) {
                        fontName = fName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (typeface != null) {
                    loads.put(fontName, typeface);
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
}
