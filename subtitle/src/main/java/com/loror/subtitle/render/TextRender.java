package com.loror.subtitle.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.loror.subtitle.SubtitlesDecoder;
import com.loror.subtitle.model.SubtitlesModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextRender {

    public static final String TAG = "TextRender";

    private final Map<Integer, List<SubtitlesModel>> showModels = new HashMap<>();
    private CharSequence simpleText;
    private final StyledLayout styledLayout = new StyledLayout();
    private final StyledPaint styledPaint = new StyledPaint();
    private final VectorPaint vectorPaint = new VectorPaint();
    private final int boxSplit = 1;//九宫格占位比例
    private final int lineSplit = 2;//空白行占位比例
    private final int lineSpace;//行间距
    private boolean drawSimpleTextBySystem = true;//是否使用系统方式（TextView）绘制简单文本
    private boolean yieldPosition;//绘制绝对坐标字幕是否避让边界
    private int leftSpace;//左部字幕避让位置
    private int topSpace;//顶部字幕避让位置
    private int rightSpace;//右部字幕避让位置
    private int bottomSpace;//底部字幕避让位置
    private float aspectRatio = 16f / 9;//视频宽高比
    private boolean aspectRatioSet;//视频宽高比是否设置
    private boolean locateInVideo;//字幕显示到视频范围（影响上下字幕）
    private boolean notRenderBeforeAspectRatioSet;//视频比例设置前不渲染
    private int width, height;//绘制宽高

    public TextRender(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public Paint getTextPaint() {
        return styledPaint.getTextPaint();
    }

    /**
     * 清空显示
     */
    public void clearText() {
        showModels.clear();
        simpleText = null;
    }

    /**
     * 显示单行
     */
    public void setText(CharSequence text) {
        styledPaint.setPtSize(0, 0);
        showModels.clear();
        simpleText = text;
    }

    /**
     * 显示一组字幕
     */
    public void setModels(SubtitlesRender render) {
        simpleText = null;
        showModels.clear();
        Map<Integer, List<SubtitlesModel>> models = render.render();
        if (models != null) {
            showModels.putAll(models);
        }
        styledPaint.setPtSize(render.playResX, render.playResY);
    }

    /**
     * 绘制字幕
     */
    public void render(@NonNull Canvas canvas) {
        if (notRenderBeforeAspectRatioSet && !aspectRatioSet) {
            return;
        }
        Rect r = canvas.getClipBounds();
        this.width = r.width();
        this.height = r.height();
        styledPaint.setScreenSize(width, height);
        vectorPaint.setScreenSize(width, height);
        drawText(canvas);
    }

    /**
     * |\–	字幕靠左	字幕居中	字幕靠右
     * 顶部	{\an7}    {\an8}    {\an9}
     * 中间	{\an4}    {\an5}    {\an6}
     * 底部	{\an1}    {\an2}    {\an3}
     */
    private void drawText(@NonNull Canvas canvas) {
        if (simpleText != null) {
            if (drawSimpleTextBySystem && simpleText instanceof Spanned) {
                styledPaint.getTextPaint().setColor(Color.WHITE);
                styledPaint.getTextPaint().setTextSize(styledPaint.pt2Px(styledPaint.getDefaultFontSize()));
                styledPaint.getTextPaint().setShadowLayer(3, 0, 0, Color.BLACK);
                StaticLayout staticLayout = new StaticLayout(simpleText, styledPaint.getTextPaint(), width, Layout.Alignment.ALIGN_CENTER, 1.0f, lineSpace, false);
                canvas.save();
                canvas.translate(0, height - staticLayout.getHeight() - 3 - bottomSpace);
                staticLayout.draw(canvas);
                canvas.restore();
            } else {
                float measureHeight = styledPaint.descent() - styledPaint.ascent() - 3 - bottomSpace;
                List<CharSequence> lines = pageLines(simpleText, null, 1);
                for (int i = 0; i < lines.size(); i++) {
                    CharSequence text = lines.get(i);
                    float measureWidth = styledPaint.getTextPaint().measureText(text, 0, text.length());
                    float x = (width - measureWidth) / 2;
                    float y = height - styledPaint.getTextPaint().descent() - ((lines.size() - 1 - i) * measureHeight);
                    y -= i * lineSpace;
                    styledPaint.drawText(canvas, text, x, y, null);
                }
            }
            return;
        }
        if (showModels.isEmpty()) {
            return;
        }
        List<SubtitlesModel> an1 = showModels.get(SubtitlesDecoder.GRAVITY_AN1);
        if (an1 != null) {
            drawBottom(an1, canvas, Gravity.LEFT);
        }
        List<SubtitlesModel> an2 = showModels.get(SubtitlesDecoder.GRAVITY_AN2);
        if (an2 != null) {
            drawBottom(an2, canvas, Gravity.CENTER);
        }
        List<SubtitlesModel> an3 = showModels.get(SubtitlesDecoder.GRAVITY_AN3);
        if (an3 != null) {
            drawBottom(an3, canvas, Gravity.RIGHT);
        }

        List<SubtitlesModel> an4 = showModels.get(SubtitlesDecoder.GRAVITY_AN4);
        if (an4 != null) {
            drawAn(an4, canvas, Gravity.LEFT, Gravity.CENTER);
        }
        List<SubtitlesModel> an5 = showModels.get(SubtitlesDecoder.GRAVITY_AN5);
        if (an5 != null) {
            drawAn(an5, canvas, Gravity.CENTER, Gravity.CENTER);
        }
        List<SubtitlesModel> an6 = showModels.get(SubtitlesDecoder.GRAVITY_AN6);
        if (an6 != null) {
            drawAn(an6, canvas, Gravity.RIGHT, Gravity.CENTER);
        }

        List<SubtitlesModel> an7 = showModels.get(SubtitlesDecoder.GRAVITY_AN7);
        if (an7 != null) {
            drawAn(an7, canvas, Gravity.LEFT, Gravity.TOP);
        }
        List<SubtitlesModel> an8 = showModels.get(SubtitlesDecoder.GRAVITY_AN8);
        if (an8 != null) {
            drawAn(an8, canvas, Gravity.CENTER, Gravity.TOP);
        }
        List<SubtitlesModel> an9 = showModels.get(SubtitlesDecoder.GRAVITY_AN9);
        if (an9 != null) {
            drawAn(an9, canvas, Gravity.RIGHT, Gravity.TOP);
        }

        List<SubtitlesModel> un = showModels.get(SubtitlesDecoder.GRAVITY_UNSET);
        if (un != null) {
            drawAbs(un, canvas);
        }
    }

    /**
     * 绘制绝对位置
     */
    private void drawAbs(List<SubtitlesModel> subtitlesModels, @NonNull Canvas canvas) {
        for (int j = 0; j < subtitlesModels.size(); j++) {
            SubtitlesModel subtitlesModel = subtitlesModels.get(j);
            Style extra = subtitlesModel.style;
            if (extra != null) {
                if (extra.isPath) {
//                    vectorPaint.drawVector(subtitlesModel, canvas);
                    continue;
                }
                float savedFontScale = extra.currentFontScale;
                Rect area = getDrawArea(extra);
                if (extra.currentClip != null) {
                    //绘制区域不可见，不绘制
                    if (extra.currentClip.left == extra.currentClip.right || extra.currentClip.top == extra.currentClip.bottom) {
                        continue;
                    }
                    //注意：extra是复用对象，多线程执行render可能将其渲染回1
                    //字高根据实际视频高度计算，当前视频使用定高宽，绘制字体偏大，缩小避免绘制超出clip区域
                    extra.currentFontScale = extra.currentFontScale * Math.min(1f, (width * 1f / height) / aspectRatio);
                }
                styledPaint.setMode(StyledPaint.MODE_BODY);
                styledPaint.setPaint(extra);
                float x = extra.x * area.width() + area.left;
//                float x = extra.x * getWidth();
                List<CharSequence> lines = styledLayout.pageLines(subtitlesModel.text(), styledPaint, extra, width, 1);
                float y = extra.y * area.height() + area.top;
                float offsetY = 0;
                float totalHeight = measureHeight(lines, extra);
                if (lines.size() > 1) {
                    //所需绘制文字总高超过屏幕，缩放到屏幕能容纳
                    if (yieldPosition && totalHeight > height) {
                        float saveExtraFontScale = styledPaint.extraFontScale;
                        float needMeasureHeight = totalHeight;
                        styledPaint.extraFontScale = saveExtraFontScale * (height / needMeasureHeight);
                        styledPaint.setMode(StyledPaint.MODE_BODY);
                        styledPaint.setPaint(extra);
                        styledPaint.extraFontScale = saveExtraFontScale;
                        totalHeight = measureHeight(lines, extra);
                    }
                }
                //字幕行的行对齐方式决定了位置设定的参考点。
                //举例来说，当行对齐设定为左上时，字幕行的左上角会被放置在 \pos 指定的位置，对于底部中间对齐来说，字幕的底部中间位置将会被放置在指定的坐标上。
                float diff;
                if (extra.gravity == SubtitlesDecoder.GRAVITY_AN7 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
                    //顶部
                    diff = 0;
                } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN1 || extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN3) {
                    //底部
                    diff = totalHeight;
                } else if (extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
                    diff = 0;
                } else {
                    //中部
                    diff = totalHeight / 2;
                }
                offsetY -= diff;
                boolean saved = updateCanvas(canvas, area, lines, x, y, offsetY, totalHeight, extra);
                if (extra.currentScaleX != 1) {
                    styledPaint.getTextPaint().setTextScaleX(extra.currentScaleX);
                } else {
                    styledPaint.getTextPaint().setTextScaleX(1);
                }
                for (int i = 0; i < lines.size(); i++) {
                    CharSequence text = lines.get(i);
                    float measureWidth = styledPaint.measureText(text, 0, text.length(), extra);
                    float finalX;
                    if (extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
                        finalX = x - measureWidth / 2;
                    } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN3 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
                        finalX = x - measureWidth;
                    } else {
                        finalX = x;
                    }
                    if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                        y += styledPaint.measureHeight(text, extra) / lineSplit + lineSpace;
                    } else {
                        float lineHeight = styledPaint.measureHeight(text, extra);
                        y += lineHeight;
                        styledPaint.drawText(canvas, text, finalX, y + offsetY - styledPaint.descent(), extra);
                        y += lineSpace;
                    }
                }
                styledPaint.getTextPaint().setTextScaleX(1);
                extra.currentFontScale = savedFontScale;
                if (saved) {
                    try {
                        //投影仪抛出异常，投影仪degree为0.00时候save判断未进入
                        //这里判断（degree != 0）有概率进入，改为判断自定义变量
                        canvas.restore();
                    } catch (Exception e) {
                        Log.e("Canvas", "restore failed(" + extra.currentDegree + "," + saved + "):" + subtitlesModel.content + "\n", e);
                    }
                }
            }
        }
    }

    /**
     * 画布变化相关标签
     */
    private boolean updateCanvas(Canvas canvas, Rect area, List<CharSequence> lines, float x, float y, float offsetY, float totalHeight, Style extra) {
        boolean saved = false;
        if (extra.currentClip != null) {
            Rect clip = clipRect(extra, area, lines.size());
            canvas.save();
            saved = true;
            canvas.clipRect(clip);
        }
        if (extra.currentDegree != 0) {
            if (!saved) {
                canvas.save();
                saved = true;
            }
            float degree = -extra.currentDegree;
            canvas.rotate(degree, x, y + offsetY + totalHeight / 2);
        }
        if (extra.currentDegreeX != 0 || extra.currentDegreeY != 0) {
            if (!saved) {
                canvas.save();
                saved = true;
            }
            float xd = (float) Math.cos(Math.toRadians(extra.currentDegreeY));
            float yd = (float) Math.cos(Math.toRadians(extra.currentDegreeX));
            float totalWidth = measureWidth(lines, extra);
            float finalX;
            if (extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
                finalX = x;
            } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN3 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
                finalX = x - totalWidth / 2;
            } else {
                finalX = x + totalWidth / 2;
            }
            canvas.scale(xd, yd, finalX, y + offsetY + totalHeight);
        }
        return saved;
    }

    /**
     * 获取clip范围
     */
    private Rect clipRect(Style clip, Rect area, int line) {
        if (clip == null) {
            return null;
        }
        //竖屏字体放大，坐标不能完全重合，取消clip
//        if (getWidth() * 1f / getHeight() < 1.4) {
//            return null;
//        }
        SubtitlesModel.Extent in = clip.currentClip;
//        float diff = in.left * getWidth() - in.left * area.height();
        float diff = in.left;
        float extraHeight = Math.max(line - 1, 0) * lineSpace;
        return new Rect(Math.max((int) (in.left * width - diff), 0),
                (int) (in.top * area.height() + area.top - extraHeight),
                Math.min((int) (in.right * width + diff), width),
                (int) (in.bottom * area.height() + area.top + extraHeight));
    }

    /**
     * 绘制顶部文字an7、an8、an9
     * 绘制中间文字an4、an5、an6
     */
    private void drawAn(List<SubtitlesModel> subtitlesModels, @NonNull Canvas canvas, int xGravity, int yGravity) {
        float bottom = -1;
        for (int j = 0; j < subtitlesModels.size(); j++) {
            SubtitlesModel subtitlesModel = subtitlesModels.get(j);
            Style extra = subtitlesModel.style;
            styledPaint.setMode(StyledPaint.MODE_BODY);
            styledPaint.setPaint(extra);
            List<CharSequence> lines = pageLines(subtitlesModel.text(), extra, xGravity == Gravity.CENTER ? 1 : boxSplit);
            if (bottom == -1) {
                if (yGravity == Gravity.CENTER) {
                    float totalHeight = measureHeight(lines, extra);
                    bottom = (height - totalHeight) / 2;
                } else {
                    bottom = getTopSpace(extra);
                }
            }
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float measureWidth = styledPaint.measureText(text, 0, text.length(), extra);
                float x = (width - measureWidth) / 2;
                if (xGravity == Gravity.LEFT) {
                    x = getLeftSpace(extra);
                } else if (xGravity == Gravity.RIGHT) {
                    x = width - measureWidth - getRightSpace(extra);
                }
                float lineHeight = styledPaint.measureHeight(text, extra);
                if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                    bottom += lineHeight / lineSplit;
                } else {
                    bottom += lineHeight;
                    styledPaint.drawText(canvas, text, x, bottom - styledPaint.descent(), extra);
                }
                bottom += lineSpace;
            }
        }
    }

    /**
     * 绘制底部文字an1、an2、an3，倒序绘制
     */
    private void drawBottom(List<SubtitlesModel> subtitlesModels, @NonNull Canvas canvas, int gravity) {
        float bottom = -1;
        for (int j = 0; j < subtitlesModels.size(); j++) {
            SubtitlesModel subtitlesModel = subtitlesModels.get(subtitlesModels.size() - 1 - j);
            Style extra = subtitlesModel.style;
            styledPaint.setMode(StyledPaint.MODE_BODY);
            styledPaint.setPaint(extra);
            if (bottom == -1) {
                int shadow = 3;
                if (extra != null) {
                    shadow += (int) extra.getShadowWidth();
                }
                bottom = height - getBottomSpace(extra) - shadow;
            }
            List<CharSequence> lines = pageLines(subtitlesModel.text(), extra, gravity == Gravity.CENTER ? 1 : boxSplit);
            bottom -= measureHeight(lines, extra);
            float itemBottom = bottom;
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float measureWidth = styledPaint.measureText(text, 0, text.length(), extra);
                float x = (width - measureWidth) / 2;
                if (gravity == Gravity.LEFT) {
                    x = getLeftSpace(extra);
                } else if (gravity == Gravity.RIGHT) {
                    x = width - measureWidth - getRightSpace(extra);
                }
                float lineHeight = styledPaint.measureHeight(text, extra);
                if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                    itemBottom += lineHeight / lineSplit;
                } else {
                    itemBottom += lineHeight;
                    styledPaint.drawText(canvas, text, x, itemBottom - styledPaint.descent(), extra);
                }
                itemBottom += lineSpace;
            }
        }
    }

    /**
     * 计算多行最大宽度
     */
    private float measureWidth(List<CharSequence> texts, Style topStyle) {
        float width = 0;
        for (int i = 0; i < texts.size(); i++) {
            CharSequence text = texts.get(i);
            float measureWidth = styledPaint.measureText(text, 0, text.length(), topStyle);
            width = Math.max(width, measureWidth);
        }
        return width;
    }

    /**
     * 计算多行高度
     */
    private float measureHeight(List<CharSequence> texts, Style topStyle) {
        float height = 0;
        for (int i = 0; i < texts.size(); i++) {
            CharSequence text = texts.get(i);
            float measureHeight = styledPaint.measureHeight(text, topStyle);
            if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                height += measureHeight / lineSplit;
            } else {
                height += measureHeight;
            }
            if (i != texts.size() - 1) {
                height += lineSpace;
            }
        }
        return height;
    }

    private float getLeftSpace(Style style) {
        if (!locateInVideo || !aspectRatioSet || style == null) {
            return leftSpace;
        }
        Rect area = getDrawArea(style);
        float scale = style.playResX == 0 ? 1 : width * 1f / style.playResX;
        return area.left + style.getMarginL() * scale;
    }

    private float getTopSpace(Style style) {
        if (!locateInVideo || !aspectRatioSet || style == null) {
            return topSpace;
        }
        float scale = style.playResY == 0 ? 1 : height * 1f / style.playResY;
        Rect area = getDrawArea(style);
        return area.top + style.getMarginV() * scale;
    }

    private float getRightSpace(Style style) {
        if (!locateInVideo || !aspectRatioSet || style == null) {
            return rightSpace;
        }
        Rect area = getDrawArea(style);
        float scale = style.playResX == 0 ? 1 : width * 1f / style.playResX;
        return width - area.right + style.getMarginR() * scale;
    }

    private float getBottomSpace(Style style) {
        if (!locateInVideo || !aspectRatioSet) {
            return bottomSpace;
        }
        Rect area = getDrawArea(style);
        //srt无底部边距定义，视频内显示使用固定边距
        if (style == null || !style.hasMarginV()) {
            return height - area.bottom + (10 * (height / 1080f));
        }
        float scale = style.playResY == 0 ? 1 : height * 1f / style.playResY;
        float space = style.getMarginV() * scale;
        return height - area.bottom + space;
    }

    private final Rect rect = new Rect();

    /**
     * 获取实际绘制区域
     */
    private Rect getDrawArea(Style style) {
        rect.top = 0;
        rect.left = 0;
        //默认根据视频宽高确定区域
        if (aspectRatioSet) {
            //实际显示宽屏，宽需要重定
            if (width * 1f / height > aspectRatio) {
                rect.bottom = height;
                int styleWidth = (int) ((aspectRatio / (width * 1f / height)) * width);
                if (styleWidth <= 0) {
                    styleWidth = width;
                }
                rect.left = (width - styleWidth) / 2;
                rect.right = rect.left + styleWidth;
            } else {
                rect.right = width;
                int styleHeight = (int) (1 / aspectRatio / (height * 1f / width) * height);
                if (styleHeight <= 0) {
                    styleHeight = height;
                }
                rect.top = (height - styleHeight) / 2;
                rect.bottom = rect.top + styleHeight;
            }
        } else {
            //默认样式，使用画布大小
            if (style == null || (style.playResX == 384 && style.playResY == 288)) {
                rect.right = width;
                rect.bottom = height;
            } else {
                //实际显示宽屏，宽需要重定
                if (width * 1f / height > style.playResX * 1f / style.playResY) {
                    rect.bottom = height;
                    int styleWidth = (int) ((style.playResX / (width * 1f / height * style.playResY)) * width);
                    if (styleWidth <= 0) {
                        styleWidth = width;
                    }
                    rect.left = (width - styleWidth) / 2;
                    rect.right = rect.left + styleWidth;
                } else {
                    rect.right = width;
                    int styleHeight = (int) ((style.playResY / (height * 1f / width * style.playResX)) * height);
                    if (styleHeight <= 0) {
                        styleHeight = height;
                    }
                    rect.top = (height - styleHeight) / 2;
                    rect.bottom = rect.top + styleHeight;
                }
            }
        }
        return rect;
    }

    private List<CharSequence> pageLines(CharSequence text, Style style, int split) {
        return styledLayout.pageLines(text, styledPaint, style, width, split);
    }

    /**
     * 设置是否使用系统方式（TextView）绘制简单文本
     */
    public void setDrawSimpleTextBySystem(boolean drawSimpleTextBySystem) {
        this.drawSimpleTextBySystem = drawSimpleTextBySystem;
    }

    /**
     * 设置字体缩放
     */
    public void setFontScale(float scale) {
        styledPaint.setFontScale(scale);
    }

    public void setYieldPosition(boolean yieldPosition) {
        this.yieldPosition = yieldPosition;
    }

    /**
     * 设置左部字幕避让位置
     */
    public void setLeftSpace(int leftSpace) {
        this.leftSpace = leftSpace;
    }

    /**
     * 设置顶部字幕避让位置
     */
    public void setTopSpace(int topSpace) {
        this.topSpace = topSpace;
    }

    /**
     * 设置右部字幕避让位置
     */
    public void setRightSpace(int rightSpace) {
        this.rightSpace = rightSpace;
    }

    /**
     * 设置底部字幕避让位置
     */
    public void setBottomSpace(int bottomSpace) {
        this.bottomSpace = bottomSpace;
    }

    /**
     * 设置字幕边距，不会影响绝对位置字幕坐标
     */
    public void setSpace(int leftSpace, int topSpace, int rightSpace, int bottomSpace) {
        this.leftSpace = leftSpace;
        this.topSpace = topSpace;
        this.rightSpace = rightSpace;
        this.bottomSpace = bottomSpace;
    }

    /**
     * 设置视频宽高比
     */
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.aspectRatioSet = true;
    }

    /**
     * 重置视频宽高比
     */
    public void resetAspectRatio() {
        this.aspectRatio = 16f / 9;
        this.aspectRatioSet = false;
    }

    /**
     * 字幕显示到视频范围
     */
    public void setLocateInVideo(boolean locateInVideo) {
        this.locateInVideo = locateInVideo;
    }

    /**
     * 视频比例设置前不渲染
     */
    public void setNotRenderBeforeAspectRatioSet(boolean notRenderBeforeAspectRatioSet) {
        this.notRenderBeforeAspectRatioSet = notRenderBeforeAspectRatioSet;
    }
}
