package com.loror.subtitle.render;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.IntDef;

import com.loror.subtitle.util.SubColorUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StyledPaint {

    public static final int MODE_BODY = 0;
    public static final int MODE_BORD = 1;
    public static final int MODE_SHADOW = 2;
    public static final int MODE_MEASURE = 3;

    @IntDef(value = {MODE_BODY, MODE_BORD, MODE_SHADOW, MODE_MEASURE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private int mode = MODE_BODY;
    private final TextPaint textPaint = new TextPaint();
    private final Paint backgroundPaint = new Paint();
    private final Paint.FontMetrics metrics = new Paint.FontMetrics();
    private int screenWidth = 1920;
    private int screenHeight = 1080;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float extraFontScale;//二级字体大小缩放
    private float fontScale = 1f;//字体大小缩放
    private int strokeScale = 2;
    private boolean roundStroke;
    private int ptWidth;
    private int ptHeight;
    private boolean eachFallMode;//单字旋转
    private boolean useCoverMode = true;

    private final PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final PorterDuffXfermode coverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    public StyledPaint() {
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
        float size = textPaint.getTextSize();
        textPaint.setTextSize(100);
        textPaint.getFontMetrics(metrics);
        textPaint.setTextSize(size);
//        float act = metrics.descent - metrics.ascent;
        float act = metrics.bottom - metrics.top;
        extraFontScale = 100 / act;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }

    /**
     * 设置view宽高
     */
    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * 设置视频宽高
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    /**
     * 设置字幕定义宽高
     */
    public void setPtSize(int ptWidth, int ptHeight) {
        this.ptWidth = ptWidth;
        this.ptHeight = ptHeight;
    }

    /**
     * 设置字体大小缩放
     */
    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    /**
     * 设置单字旋转模式
     */
    public void setEachFallMode(boolean eachFallMode) {
        this.eachFallMode = eachFallMode;
    }

    public float getDefaultFontSize() {
        if (ptWidth == 0) {
            return 20;
        }
        return ptWidth / 384f * 20;
    }

    public void setStrokeScale(int strokeScale) {
        this.strokeScale = strokeScale;
    }

    public void setRoundStroke(boolean roundStroke) {
        this.roundStroke = roundStroke;
    }

    public TextPaint getTextPaint() {
        return textPaint;
    }

    public float getPaintHeight() {
        return descent() - ascent();
    }

    public float descent() {
//        return textPaint.descent();
        textPaint.getFontMetrics(metrics);
        return metrics.bottom;
    }

    public float ascent() {
//        return textPaint.ascent();
        textPaint.getFontMetrics(metrics);
        return metrics.top;
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setTextSize(float size) {
        textPaint.setTextSize(size);
    }

    /**
     * ass字幕字体大小为pt
     * 字幕定义为字高
     */
    public float pt2Px(float pt) {
        int width = screenWidth;
        int height = screenHeight;
        if (width == 0 || height == 0) {
            return pt;
        }
        if (ptHeight != 0) {
            float scale = height * 1f / ptHeight;
            return pt * scale * fontScale * extraFontScale;
        }
        float scale = height * 1f / 288;//字幕默认定义384*288
        return pt * scale * fontScale * extraFontScale;
    }

    /**
     * 根据style设置画笔
     * 返回阴影深度
     */
    public float setPaint(Style extra) {
        if (extra == null) {
            float size = pt2Px(getDefaultFontSize());
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setTextSize(size);
            textPaint.setStrokeWidth(2);
            textPaint.setFakeBoldText(false);
            textPaint.setUnderlineText(false);
            textPaint.setStrikeThruText(false);
            textPaint.setTextSkewX(0);
            textPaint.setLetterSpacing(0);
            if (mode == MODE_SHADOW) {
                textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                textPaint.setColor(0xAA000000);
                textPaint.setShadowLayer(3, 0, 0, 0xAA000000);
                return 1.5f;
            } else if (mode == MODE_BORD) {
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setColor(Color.BLACK);
                textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
            } else {
                textPaint.setStyle(Paint.Style.FILL);
                textPaint.setColor(Color.WHITE);
                textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
            }
        } else {
            Typeface typeface = FontRender.findTypeface(extra.getFont());
            if (typeface != null) {
                textPaint.setTypeface(typeface);
            } else {
                textPaint.setTypeface(Typeface.DEFAULT);
            }
            if (extra.hasFontSize()) {
                float size = pt2Px(extra.getFontSize());
                textPaint.setTextSize(size);
            } else {
                float size = pt2Px(getDefaultFontSize());
                textPaint.setTextSize(size);
            }
            if (extra.currentFontScale != 1) {
                textPaint.setTextSize(textPaint.getTextSize() * extra.currentFontScale);
            }
            int fontColor = extra.getFontColor();
            if (extra.useSecond && extra.hasFontSecondaryColor()) {
                fontColor = extra.getFontSecondaryColor();
            }
            int borderColor = Color.BLACK;
            if (extra.hasBorderColor()) {
                borderColor = extra.getBorderColor();
            }
            int shadowColor = Color.BLACK;
            if (extra.hasShadowColor()) {
                shadowColor = extra.getShadowColor();
            }
            float shadowWidth = 1;
            if (extra.hasShadowWidth()) {
                shadowWidth = extra.getShadowWidth();
            }
            float borderWidth = 2f;
            if (extra.hasBorderWidth()) {
                borderWidth = extra.getBorderWidth();
            }
            //缩放模式按照字幕定义宽高缩放，非缩放模式按照视频大小缩放
            if (extra.scaleBS) {
                if (ptWidth > 0) {
                    shadowWidth = shadowWidth * screenWidth / ptWidth;
                    borderWidth = borderWidth * screenWidth / ptWidth;
                }
            } else if (videoWidth != 0) {
                shadowWidth = shadowWidth * screenWidth / videoWidth;
                borderWidth = borderWidth * screenWidth / videoWidth;
            }
            if (borderWidth > 0) {
                borderWidth = borderWidth * strokeScale;
            }
            textPaint.setStrokeWidth(borderWidth);
            float spacing = 0f;
            if (extra.hasSpace()) {
                spacing = extra.getSpace();
            }
            textPaint.setLetterSpacing(pt2Px(spacing) / textPaint.getTextSize());
            if (extra.hasBold()) {
                textPaint.setFakeBoldText(extra.isBold());
            }
            if (extra.hasItalic()) {
                textPaint.setTextSkewX(extra.isItalic() ? -0.25f : 0);
            }
            if (extra.hasUnderline()) {
                textPaint.setUnderlineText(extra.isUnderline());
            }
            if (extra.hasStrikeout()) {
                textPaint.setStrikeThruText(extra.isStrikeout());
            }
            textPaint.setStrokeJoin(Paint.Join.MITER);
            if (mode == MODE_SHADOW) {
                float fontAlpha = Color.alpha(fontColor);
                if (fontAlpha == 0) {
                    textPaint.setStyle(Paint.Style.STROKE);
                } else {
                    textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                }
                if (extra.alpha != 1f) {
                    shadowColor = SubColorUtil.replaceColorAlpha(shadowColor, (int) (255 * extra.alpha * 0.6f));
                }
                textPaint.setColor(shadowColor);
                if (shadowWidth == 0) {
                    textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                } else {
                    textPaint.setShadowLayer(1, 0, 0, shadowColor);
                }
                return shadowWidth;
            } else if (mode == MODE_BORD) {
                textPaint.setStyle(Paint.Style.STROKE);
                if (extra.alpha != 1f) {
                    borderColor = SubColorUtil.replaceColorAlpha(borderColor, (int) (255 * extra.alpha));
                }
                if (roundStroke) {
                    textPaint.setStrokeJoin(Paint.Join.ROUND);
                }
                textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                textPaint.setColor(borderColor);
            } else {
                textPaint.setStyle(Paint.Style.FILL);
                textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                if (extra.alpha != 1f) {
                    textPaint.setColor(SubColorUtil.replaceColorAlpha(fontColor, extra.alpha));
                } else {
                    textPaint.setColor(fontColor);
                }
            }
        }
        return 0;
    }

    /**
     * 获取单行高度
     */
    public float measureHeight(CharSequence text, Style topStyle) {
        if (TextUtils.isEmpty(text)) {
            return descent() - ascent();
        }
        setMode(MODE_MEASURE);
        setPaint(topStyle);
        if (text instanceof Spanned) {
            float height = 0;
            Spanned spanned = (Spanned) text;
            int next;
            float saveSize = textPaint.getTextSize();
            for (int i = 0; i < spanned.length(); i = next) {
                next = spanned.nextSpanTransition(i, spanned.length(), CharacterStyle.class);
                StyledSpan[] spans = spanned.getSpans(i, next, StyledSpan.class);
                if (spans.length > 0) {
                    StyledSpan styledSpan = spans[0];
                    Style style = styledSpan.getStyle();
                    if (style != null) {
                        setMode(MODE_MEASURE);
                        setPaint(style);
                        height = Math.max(height, descent() - ascent());
                    }
                }
            }
            textPaint.setTextSize(saveSize);
            return height != 0 ? height : (descent() - ascent());
        } else {
            if (topStyle != null && topStyle.isFontFall()) {
                return measureFallHeight(text, topStyle);
            }
            return descent() - ascent();
        }
    }

    private float measureFallHeight(CharSequence text, Style topStyle) {
        int textLength = text.length();
        float max = 0;
        for (int i = 0; i < textLength; i++) {
            char c = text.charAt(i);
            float measure = textPaint.measureText(String.valueOf(c));
            if (measure > max) {
                max = measure;
            }
        }
        return max;
    }

    /**
     * 获取绘制宽度
     */
    public float measureText(CharSequence text, int start, int end, Style topStyle) {
        //render渲染字体动画在顶部style中
        setMode(MODE_MEASURE);
        setPaint(topStyle);
        if (text instanceof Spanned) {
            float saveSize = textPaint.getTextSize();
            Spanned spanned = (Spanned) text;
            int next;
            float xStart = 0;
            float xEnd = 0;
            for (int i = start; i < Math.min(spanned.length(), end); i = next) {
                next = spanned.nextSpanTransition(i, spanned.length(), CharacterStyle.class);
                StyledSpan[] spans = spanned.getSpans(i, next, StyledSpan.class);
                if (spans.length > 0) {
                    StyledSpan styledSpan = spans[0];
                    Style style = styledSpan.getStyle();
                    setMode(MODE_MEASURE);
                    setPaint(style);
                }
                xEnd = xStart + textPaint.measureText(spanned, i, next);
                xStart = xEnd;
            }
            textPaint.setTextSize(saveSize);
            return xEnd;
        } else {
            if (topStyle != null && topStyle.isFontFall()) {
                return text.length() * (descent() - ascent());
            }
            return textPaint.measureText(text, start, end);
        }
    }

    /**
     * 绘制text，支持StyledSpan、ForegroundColorSpan
     * 返回本次绘制高度
     */
    public float drawText(Canvas canvas, CharSequence text, float x, float y, Style topStyle) {
        float height = 0;
        int start = 0;
        int end = text.length();
        if (start >= text.length()) {
            return descent() - ascent();
        }
        setMode(MODE_MEASURE);
        setPaint(topStyle);
        Style current = topStyle;
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            int next;
            float xStart = 0;
            float xEnd;
            for (int i = start; i < Math.min(spanned.length(), end); i = next) {
                next = spanned.nextSpanTransition(i, spanned.length(), CharacterStyle.class);
                StyledSpan[] spans = spanned.getSpans(i, next, StyledSpan.class);
                CharacterStyle[] characterStyles = spanned.getSpans(i, next, CharacterStyle.class);
                if (spans.length > 0) {
                    StyledSpan styledSpan = spans[0];
                    Style style = styledSpan.getStyle();
                    if (style != null) {
                        current = style;
                        setMode(MODE_MEASURE);
                        setPaint(style);
                    }
                }
                height = Math.max(height, descent() - ascent());
                if (current != null && current.isFontFall()) {
                    float textWidth = drawFallText(canvas, text.subSequence(i, next), xStart + x, y, current, characterStyles);
                    xEnd = xStart + textWidth + (textPaint.getLetterSpacing() * textPaint.getTextSize());
                    xStart = xEnd;
                } else {
                    float textWidth = textPaint.measureText(spanned, i, next);
                    drawText(canvas, spanned, i, next, xStart + x, y, textWidth, current, characterStyles);
                    xEnd = xStart + textWidth + (textPaint.getLetterSpacing() * textPaint.getTextSize());
                    xStart = xEnd;
                }
            }
        } else {
            if (topStyle != null && topStyle.isFontFall()) {
                drawFallText(canvas, text, x, y, topStyle, null);
            } else {
                float textWidth = textPaint.measureText(text, start, end);
                drawText(canvas, text, start, end, x, y, textWidth, current, null);
            }
        }
        return height != 0 ? height : (descent() - ascent());
    }

    /**
     * 绘制倾倒字体
     */
    private float drawFallText(Canvas canvas, CharSequence text, float x, float y, Style style, CharacterStyle[] characterStyles) {
        float width;
        if (eachFallMode) {
            float textWidth = textPaint.measureText(text, 0, text.length());
            float xi = x;
            float hi = descent() - ascent();
            for (int i = 0; i < text.length(); i++) {
                int count = canvas.save();
                float wi = textPaint.measureText(text, 0, 1);
                canvas.rotate(-90, xi + wi / 2, y + descent() - hi / 2);
                drawText(canvas, text, i, i + 1, xi, y, wi, style, characterStyles);
                canvas.restoreToCount(count);
                xi += wi;
            }
            width = textWidth - (textPaint.getLetterSpacing() * textPaint.getTextSize());
        } else {
            //倾倒文字，缩小大小范围
            float measureHeight = textPaint.descent() - textPaint.ascent();
            int textLength = text.length();
            float[] measures = new float[textLength];
            float max = 0;
            for (int i = 0; i < textLength; i++) {
                char c = text.charAt(i);
                measures[i] = textPaint.measureText(String.valueOf(c));
                if (measures[i] > max) {
                    max = measures[i];
                }
            }
            float descent = descent();
            y += descent;
            float space = textPaint.getLetterSpacing() * textPaint.getTextSize();
            float wi = (measureHeight * textLength) + (space * (textLength - 1));
            float px = x + wi / 2f;
            float py = y - max / 2;
            int count = canvas.save();
            canvas.rotate(-90, px, py);
            for (int i = 0; i < textLength; i++) {
                char c = text.charAt(i);
                float xi = px - measures[i] / 2f;
                float yi = py - wi / 2f + measureHeight * (i + 1);
                if (i > 0) {
                    yi += i * space;
                }
                drawText(canvas, String.valueOf(c), 0, 1, xi, yi - descent, measureHeight, style, characterStyles);
//                    canvas.drawText(String.valueOf(c), px - measures[i] / 2f, py + (i + 1 - (textLength / 2f)) * measureHeight, paint);
            }
            canvas.restoreToCount(count);
            width = wi;
        }
        return width;
    }

    /**
     * 绘制文字阴影、边框、主体
     */
    private void drawText(Canvas canvas, CharSequence text, int start, int end, float x, float y, float textWidth, Style style, CharacterStyle[] characterStyles) {
        int drawCount = 0;
        int borderStyle = style == null ? 1 : style.getBorderStyle();
        boolean isBe = style != null && style.getBe() > 0;
        boolean isScaleBs = style != null && style.scaleBS;
        boolean saveLayer = false;
        int layer = 0;
        if (borderStyle != 3) {
            this.setMode(StyledPaint.MODE_SHADOW);
            float shadowDeep = this.setPaint(style);
            applySpan(characterStyles);
            float be = 0;
            if (isBe) {
                be = style.getBe();
                if (be == 1) {
                    be = textPaint.getTextSize() / 20f;
                } else {
                    be = isScaleBs ? pt2Px(be) : be;
                }
            }
            if (be == 0 && style != null && style.currentBlur > 0) {
                be = style.currentBlur;
            }
            if (be > 0) {
                textPaint.setMaskFilter(new BlurMaskFilter(be, BlurMaskFilter.Blur.OUTER));
            }
            if (shadowDeep > 0) {
                int alpha = Color.alpha(textPaint.getColor());
                //不可见不绘制
                if (alpha > 5) {
                    canvas.drawText(text, start, end, x + shadowDeep + 1, y + shadowDeep, textPaint);
                    drawCount++;
                }
            }
            if (be > 0) {
                textPaint.setMaskFilter(new BlurMaskFilter(be, BlurMaskFilter.Blur.SOLID));
            }
            float strokeWidth = textPaint.getStrokeWidth();
            //strokeWidth设置为0，字体边框仍然会绘制，直接不执行绘制
            if (strokeWidth > 0) {
                this.setMode(StyledPaint.MODE_BORD);
                this.setPaint(style);
                applySpan(characterStyles);
                int alpha = Color.alpha(textPaint.getColor());
                if (alpha > 5) {
                    if (drawCount > 0) {
                        //内边框需要裁剪,阴影不需要裁剪
                        layer = canvas.saveLayer(x - strokeWidth, y + ascent() - strokeWidth, x + textWidth + strokeWidth, y + descent() + strokeWidth, textPaint);
                        saveLayer = true;
                    }
//                if (drawCount > 0 && !isBe) {
//                    textPaint.setXfermode(clearMode);
//                    canvas.drawText(text, start, end, x, y, textPaint);
//                    textPaint.setXfermode(null);
//                }
                    canvas.drawText(text, start, end, x, y, textPaint);
                    drawCount++;
                }
            }
        } else {
            //style3绘制背景
            this.setMode(StyledPaint.MODE_BORD);
            this.setPaint(style);
            backgroundPaint.setColor(textPaint.getColor());
            canvas.drawRect(x, y + ascent(), x + measureText(text, start, end, style), y + descent(), backgroundPaint);
        }
        this.setMode(StyledPaint.MODE_BODY);
        this.setPaint(style);
        ForegroundColorSpan foregroundColorSpan = applySpan(characterStyles);
        textPaint.setMaskFilter(null);
        if (saveLayer) {
            if (useCoverMode) {
                textPaint.setXfermode(coverMode);
            } else {
                textPaint.setXfermode(clearMode);
                canvas.drawText(text, start, end, x, y, textPaint);
                textPaint.setXfermode(null);
                canvas.restoreToCount(layer);
                saveLayer = false;
            }
        }
        //注意\be标签只会模糊文本的边框 ，不是整体。
        //注意\blur如果没有边框，那么文本整体就会被模糊。
        if (style != null && style.currentBlur > 0 && textPaint.getStrokeWidth() == 0) {
            textPaint.setMaskFilter(new BlurMaskFilter(pt2Px(style.currentBlur), BlurMaskFilter.Blur.NORMAL));
        }
        if (foregroundColorSpan != null) {
            if (foregroundColorSpan instanceof PartForegroundColorSpan) {
                PartForegroundColorSpan partForegroundColorSpan = (PartForegroundColorSpan) foregroundColorSpan;
                float part = partForegroundColorSpan.getLastPercent();
                float totalWidth = textPaint.measureText(text, start, end);
                float lastWidth = textPaint.measureText(text, end - 1, end) * (1 - part);
                int saveColor = textPaint.getColor();
                canvas.save();
                canvas.clipRect(new Rect(0, 0, (int) (x + totalWidth - lastWidth), screenHeight));
                textPaint.setColor(partForegroundColorSpan.getForegroundColor());
                canvas.drawText(text, start, end, x, y, textPaint);
                canvas.clipRect(new Rect((int) (x + totalWidth - lastWidth), 0, screenWidth, screenHeight));
                textPaint.setColor(saveColor);
                canvas.drawText(text, start, end, x, y, textPaint);
                canvas.restore();
            } else {
                textPaint.setColor(foregroundColorSpan.getForegroundColor());
                canvas.drawText(text, start, end, x, y, textPaint);
            }
        } else {
            canvas.drawText(text, start, end, x, y, textPaint);
        }
        if (saveLayer) {
            if (useCoverMode) {
                textPaint.setXfermode(null);
                canvas.restoreToCount(layer);
            }
        }
        textPaint.setMaskFilter(null);
    }

    private ForegroundColorSpan applySpan(CharacterStyle[] characterStyles) {
        if (characterStyles == null || characterStyles.length == 0) {
            return null;
        }
        ForegroundColorSpan foregroundColorSpan = null;
        for (CharacterStyle span : characterStyles) {
            if (span instanceof StrikethroughSpan) {
                textPaint.setStrikeThruText(true);
            } else if (span instanceof UnderlineSpan) {
                textPaint.setUnderlineText(true);
            } else if (span instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) span;
                int style = styleSpan.getStyle();
                if ((style & Typeface.BOLD) != 0) {
                    textPaint.setFakeBoldText(true);
                }
                if ((style & Typeface.ITALIC) != 0) {
                    textPaint.setTextSkewX(-0.25f);
                }
            } else if (span instanceof ForegroundColorSpan) {
                foregroundColorSpan = (ForegroundColorSpan) span;
            }
        }
        return foregroundColorSpan;
    }

    private Typeface typeface;
    private float fontSize;
    private boolean isBold;
    private boolean isUnderline;
    private boolean isStrikeout;
    private float skew;
    private int fontColor;
    private float borderWidth;

    void save(TextPaint textPaint) {
        typeface = textPaint.getTypeface();
        fontSize = textPaint.getTextSize();
        isBold = textPaint.isFakeBoldText();
        isUnderline = textPaint.isUnderlineText();
        isStrikeout = textPaint.isStrikeThruText();
        skew = textPaint.getTextSkewX();
        fontColor = textPaint.getColor();
        borderWidth = textPaint.getStrokeWidth();
    }

    void resume(TextPaint textPaint) {
        if (typeface != null) {
            textPaint.setTypeface(typeface);
        }
        textPaint.setTextSize(fontSize);
        textPaint.setTextSkewX(skew);
        textPaint.setFakeBoldText(isBold);
        textPaint.setUnderlineText(isUnderline);
        textPaint.setStrikeThruText(isStrikeout);
        textPaint.setColor(fontColor);
        textPaint.setStrokeWidth(borderWidth);
    }
}
