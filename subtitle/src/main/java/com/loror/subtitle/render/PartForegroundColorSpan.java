package com.loror.subtitle.render;

import android.os.Parcel;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

public class PartForegroundColorSpan extends ForegroundColorSpan {

    private final float lastPercent;

    public PartForegroundColorSpan(int color, float lastPercent) {
        super(color);
        this.lastPercent = lastPercent;
    }

    public PartForegroundColorSpan(@NonNull Parcel src) {
        super(src);
        lastPercent = src.readFloat();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(lastPercent);
    }

    public float getLastPercent() {
        return lastPercent;
    }
}
