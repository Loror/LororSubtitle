package com.loror.subtitle.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loror.subtitle.R;
import com.loror.subtitle.model.LrcBean;
import com.loror.subtitle.render.TimedSpan;

import java.util.ArrayList;
import java.util.List;

public class LrcView extends View {

    public static final String TAG = "LrcView";

    private CharSequence text;
    private final List<CharSequence> lines = new ArrayList<>();
    private int layoutHash;
    private int currentSeek = -1;

    private final TextPaint textPaint = new TextPaint();
    private final int mMaxLine;
    private int highlightColor;
    private boolean highlightBold;
    private float lineSpace;
    private final float textGravity;

    private long time;
    private final int indexSpec = 1000000;
    private int scrollDuration = 500;
    private boolean pressed;

    private final GestureDetector mGestureDetector;
    private final Scroller mScroller;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        mMaxLine = a.getInteger(R.styleable.LrcView_lrcMaxLine, 1);
        textGravity = a.getInteger(R.styleable.LrcView_lrcGravity, 0);
        highlightColor = a.getColor(R.styleable.LrcView_lrcHighlightTextColor, Color.BLUE);
        highlightBold = a.getBoolean(R.styleable.LrcView_lrcHighlightBold, false);
        lineSpace = a.getDimension(R.styleable.LrcView_lrcLineSpace, 5);
        float shadow = a.getDimension(R.styleable.LrcView_lrcShadow, 0);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setColor(a.getColor(R.styleable.LrcView_lrcTextColor, Color.BLACK));
        textPaint.setTextSize(a.getDimension(R.styleable.LrcView_lrcTextSize, 24));
        if (shadow != 0) {
            textPaint.setShadowLayer(shadow, 0, 0, Color.GRAY);
        }
        a.recycle();
        mGestureDetector = new GestureDetector(context, new LrcGestureListener(context));
        mScroller = new Scroller(context, new OvershootInterpolator());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutHash = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), heightMeasureSpec);
        } else {
            float lineHeight = textPaint.descent() - textPaint.ascent() + lineSpace;
            int totalHeight = (int) (lineHeight) * mMaxLine;
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), totalHeight);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(text)) {
            float width = textPaint.measureText("暂无歌词~");
            canvas.drawText("暂无歌词~", (getWidth() - width) / 2, getHeight() / 2f, textPaint);
            return;
        }
        layoutLines();
        int currentIndex = index() % indexSpec;
        float lineHeight = textPaint.descent() - textPaint.ascent() + lineSpace;
        float y = -textPaint.ascent();
        int scrollY = getScrollY();
        int height = getHeight();
        for (int i = 0, size = lines.size(); i < size; i++) {
            if (y < scrollY || y > scrollY + height + lineHeight) {
                y += lineHeight;
                continue;
            }
            CharSequence line = lines.get(i);
            float x;
            if (textGravity == 1) {
                int width = getWidth() - getPaddingLeft() - getPaddingRight();
                float textWidth = textPaint.measureText(line, 0, line.length());
                x = (width - textWidth) / 2 + getPaddingLeft();
            } else if (textGravity == 2) {
                int width = getWidth() - getPaddingLeft() - getPaddingRight();
                float textWidth = textPaint.measureText(line, 0, line.length());
                x = width - textWidth - getPaddingRight();
            } else {
                x = getPaddingLeft();
            }
            drawText(canvas, line, x, y, currentIndex);
            y += lineHeight;
        }
    }

    private void drawText(Canvas canvas, CharSequence text, float x, float y, int currentIndex) {
        int start = 0;
        int end = text.length();
        if (start >= text.length()) {
            return;
        }
        int savedColor = textPaint.getColor();
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            int next;
            float xStart = 0;
            float xEnd;
            for (int i = start; i < Math.min(spanned.length(), end); i = next) {
                next = spanned.nextSpanTransition(i, spanned.length(), CharacterStyle.class);
                ForegroundColorSpan[] fgSpans = spanned.getSpans(i, next, ForegroundColorSpan.class);
                if (fgSpans != null && fgSpans.length > 0) {
                    textPaint.setColor(fgSpans[0].getForegroundColor());
                }
                TimedSpan[] timedSpans = spanned.getSpans(0, spanned.length(), TimedSpan.class);
                if (timedSpans != null && timedSpans.length > 0) {
                    boolean highlight = timedSpans[0].getIndex() == currentIndex;
                    if (highlight) {
                        textPaint.setColor(highlightColor);
                        if (highlightBold) {
                            textPaint.setFakeBoldText(true);
                        }
                    }
                }
                float textWidth = textPaint.measureText(spanned, i, next);
                canvas.drawText(text, start, end, x, y, textPaint);
                xEnd = xStart + textWidth + (textPaint.getLetterSpacing() * textPaint.getTextSize());
                xStart = xEnd;
                textPaint.setColor(savedColor);
                textPaint.setFakeBoldText(false);
            }
        } else {
            canvas.drawText(text, start, end, x, y, textPaint);
            textPaint.setColor(savedColor);
            textPaint.setColor(savedColor);
            textPaint.setFakeBoldText(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (TextUtils.isEmpty(text)) return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();
                pressed = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                break;
        }
        if (mGestureDetector != null) mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset()) {
            int scrollY = getScrollY();
            int currY = mScroller.getCurrY();
            if (scrollY != currY) {
                scrollTo(getScrollX(), currY);
            }
            invalidate();
        }
        super.computeScroll();
    }

    /**
     * 计算换行
     */
    private void layoutLines() {
        if (TextUtils.isEmpty(text)) {
            layoutHash = 0;
            lines.clear();
            return;
        }
        int hash = text.hashCode() + text.length();
        if (hash == layoutHash) {
            return;
        }
        layoutHash = hash;
        lines.clear();
        final StaticLayout layout = new StaticLayout(text, textPaint, getWidth() - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_NORMAL,
                1.0f, 0f, false);
        int line = layout.getLineCount();
        if (line == 1) {
            lines.add(text);
        } else {
            for (int i = 0; i < line; i++) {
                CharSequence l = text.subSequence(layout.getLineStart(i), layout.getLineEnd(i));
                if (!TextUtils.isEmpty(l)) {
                    if (l.charAt(l.length() - 1) == '\n') {
                        lines.add(l.subSequence(0, l.length() - 1));
                    } else {
                        lines.add(l);
                    }
                }
            }
        }
    }

    private int index() {
        if (lines.isEmpty()) {
            return 0;
        }
        int index = 0;
        int lineIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            CharSequence line = lines.get(i);
            if (line instanceof Spanned) {
                Spanned spanned = (Spanned) line;
                TimedSpan[] timedSpans = spanned.getSpans(0, spanned.length(), TimedSpan.class);
                if (timedSpans != null && timedSpans.length > 0) {
                    TimedSpan timedSpan = timedSpans[0];
                    if (time >= timedSpan.getStart()) {
                        lineIndex = i;
                        index = timedSpan.getIndex();
                    } else {
                        break;
                    }
                }
            }
        }
        return index == -1 ? -1 : (lineIndex * indexSpec + index);
    }

    /**
     * 滚动到位置
     */
    public void smoothScrollTo(int x, int y) {
        int oldX = getScrollX();
        int oldY = getScrollY();
        mScroller.startScroll(oldX, oldY, x - oldX, y - oldY, scrollDuration);
        invalidate();
    }

    /**
     * 设置歌词
     */
    public void setLrcs(List<LrcBean> lrcs) {
//        Log.i(TAG, "setLrcs:" + lrcs.size());
        currentSeek = -1;
        layoutHash = 0;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0, size = lrcs.size(); i < size; i++) {
            LrcBean lrc = lrcs.get(i);
            String text = lrc.getLrc();
            if (text == null) {
                continue;
            }
            int start = builder.length();
            TimedSpan timedSpan = new TimedSpan(i, lrc.getBeginTime());
            builder.append(text).append("\n");
            builder.setSpan(timedSpan, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        text = builder;
        lines.clear();
        invalidate();
    }

    /**
     * 清空歌词
     */
    public void clearLrc() {
        currentSeek = -1;
        layoutHash = 0;
        if (!TextUtils.isEmpty(text)) {
            text = "";
            lines.clear();
            if (mScroller.computeScrollOffset()) {
                mScroller.abortAnimation();
            }
            scrollTo(getScrollX(), 0);
            invalidate();
        }
    }

    public void seek(long time) {
        this.time = time;
        if (lines.isEmpty()) {
            currentSeek = -1;
            scrollTo(0, 0);
            return;
        }
        int indexs = index();
        int index = indexs % indexSpec;
        int line = indexs / indexSpec;
//        Log.i(TAG, "seek:" + time + " - " + index);
        if (index != -1 && index != currentSeek) {
            if (pressed) {
                invalidate();
            } else {
                currentSeek = index;
                float lineHeight = textPaint.descent() - textPaint.ascent() + lineSpace;
                int height = getHeight();
                if (height != 0) {
                    smoothScrollTo(0, (int) Math.max((lineHeight * line) + (lineHeight / 2) - (height / 2f), 0));
                }
            }
        }
    }

    public void release() {
        currentSeek = -1;
        if (mScroller != null && mScroller.computeScrollOffset()) {
            mScroller.abortAnimation();
        }
        clearLrc();
    }

    private OnClickListener l;

    public void setOnTapClickListener(@Nullable OnClickListener l) {
        this.l = l;
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setLineSpace(float lineSpace) {
        this.lineSpace = lineSpace;
    }

    public void setScrollDuration(int scrollDuration) {
        this.scrollDuration = scrollDuration;
    }

    public class LrcGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int scaledTouchSlop;//滑动最小距离

        private float y;
        private float total;
        private float line;
        private float height;

        public LrcGestureListener(Context context) {
            this.scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            y = e.getY();
            line = textPaint.descent() - textPaint.ascent() + lineSpace;
            total = lines.size() * line;
            height = getHeight();
            return false;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            if (l != null) {
                l.onClick(LrcView.this);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (total <= 0) {
                return false;
            }
            float durY = e2.getY() - y;
            int oldY = getScrollY();
            if (Math.abs(durY) > scaledTouchSlop / 2f) {
                //上滑超出
                if (durY > 0 && oldY <= 0) {
                    return true;
                }
                //下滑超出
                if (durY < 0 && oldY > 0 && oldY - durY > total - height / 2 + line) {
                    return true;
                }
                scrollBy(getScrollX(), -(int) durY);
                y = e2.getY();
                return true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }
}
