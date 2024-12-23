package com.loror.subtitle.util;

import android.text.TextUtils;

public class TextUtil {

    public static boolean isNumber(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isInvalid(String text) {
        if (TextUtils.isEmpty(text)) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                return false;
            }
        }
        return true;
//        return TextUtils.isEmpty(text.replace(" ", "")
//                .replace("\r", "")
//                .replace("\n", "")
//                .replace("\t", ""));
    }

}
