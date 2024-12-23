package com.loror.subtitle.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loror.subtitle.render.SubtitlesRender;
import com.loror.subtitle.render.TextRender;

public class SubtitlesView extends View {

    public static final String TAG = "SubtitlesView";

    private final TextRender textRender = new TextRender(2);

    public SubtitlesView(Context context) {
        this(context, null);
    }

    public SubtitlesView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitlesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, textRender.getTextPaint());
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        textRender.render(canvas);
    }

    /**
     * 清空显示
     */
    public void clearText() {
        textRender.clearText();
        currentRender = null;
        invalidate();
    }

    /**
     * 显示单行
     */
    public void showText(CharSequence text) {
        textRender.setText(text);
        currentRender = null;
        invalidate();
    }

    private SubtitlesRender currentRender;

    public void clearRender() {
        currentRender = null;
    }

    /**
     * 显示一组字幕
     */
    public void showModels(SubtitlesRender render) {
        if (currentRender != null) {
            //字幕内容未改变，不再次渲染
            if (currentRender.equals(render)) {
                return;
            }
        }
        currentRender = render;
        textRender.setModels(render);
        invalidate();
    }

    @NonNull
    public TextRender getRender() {
        return textRender;
    }
}