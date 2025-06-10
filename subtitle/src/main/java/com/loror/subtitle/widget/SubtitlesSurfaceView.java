package com.loror.subtitle.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loror.subtitle.render.SubtitlesRender;
import com.loror.subtitle.render.TextRender;
import com.loror.subtitle.util.OneTaskExecutor;

public class SubtitlesSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "SubtitlesSurfaceView";

    private final TextRender textRender = new TextRender(2);
    private final OneTaskExecutor server = new OneTaskExecutor();
    private SurfaceHolder mSurface;

    public SubtitlesSurfaceView(Context context) {
        this(context, null);
    }

    public SubtitlesSurfaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitlesSurfaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, textRender.getTextPaint());
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
    }

    private Canvas lockCanvas(SurfaceHolder mSurface) {
        Canvas canvas;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas = mSurface.lockHardwareCanvas();
        } else {
            canvas = mSurface.lockCanvas();
        }
        return canvas;
    }

    /**
     * 清空显示
     */
    public void clearText() {
        if (mSurface == null) {
            return;
        }
        textRender.clearText();
        currentRender = null;
        server.execute(() -> {
            if (mSurface == null) {
                return;
            }
            Canvas canvas = lockCanvas(mSurface);
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSurface.unlockCanvasAndPost(canvas);
            }
        });
    }

    /**
     * 显示单行
     */
    public void showText(CharSequence text) {
        if (mSurface == null) {
            return;
        }
        textRender.setText(text);
        currentRender = null;
        server.execute(() -> {
            if (mSurface == null) {
                return;
            }
            Canvas canvas = lockCanvas(mSurface);
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                textRender.render(canvas);
                mSurface.unlockCanvasAndPost(canvas);
            }
        });
    }

    private SubtitlesRender currentRender;

    public void clearRender() {
        currentRender = null;
    }

    public void update() {
        if (mSurface == null) {
            return;
        }
        SubtitlesRender render = currentRender;
        if (render != null) {
            currentRender = null;
            showModels(render);
        }
    }

    /**
     * 显示一组字幕
     */
    public void showModels(SubtitlesRender render) {
        if (mSurface == null) {
            return;
        }
        if (currentRender != null) {
            //字幕内容未改变，不再次渲染
            if (currentRender.equals(render)) {
                return;
            }
        }
        currentRender = render;
        server.execute(() -> {
            if (mSurface == null) {
                return;
            }
            textRender.setModels(render);
            Canvas canvas = lockCanvas(mSurface);
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                textRender.render(canvas);
                mSurface.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        server.clear();
        super.onDetachedFromWindow();
    }

    @NonNull
    public TextRender getRender() {
        return textRender;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mSurface = holder;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mSurface = holder;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mSurface = null;
        server.clear();
    }
}