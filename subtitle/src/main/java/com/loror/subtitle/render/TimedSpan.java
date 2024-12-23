package com.loror.subtitle.render;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.NonNull;

public class TimedSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    private final int index;
    private final long start;

    public TimedSpan(int index, long start) {
        this.index = index;
        this.start = start;
    }

    public int getIndex() {
        return index;
    }

    public long getStart() {
        return start;
    }

    @Override
    public int getSpanTypeId() {
        return 102;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeLong(start);
    }

    @Override
    public void updateDrawState(TextPaint tp) {

    }
}
