package com.loror.subtitle.render;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

public class StyledSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    private Style style;

    public StyledSpan(Style style) {
        this.style = style;
    }

    public StyledSpan(@NonNull Parcel src) {
        String data = src.readString();
        if (!TextUtils.isEmpty(data)) {
            style = JSON.parseObject(data, Style.class);
        }
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public int getSpanTypeId() {
        return 101;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(style == null ? null : JSON.toJSONString(style));
    }

    @Override
    public void updateDrawState(TextPaint tp) {

    }
}
