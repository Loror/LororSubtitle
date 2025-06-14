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
import com.loror.subtitle.model.RenderedModel;
import com.loror.subtitle.model.SubtitlesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextRender {

    public static final String TAG = "TextRender";

    private final Map<Integer, RenderedModel> showModels = new HashMap<>();
    private CharSequence simpleText;
    private final StyledLayout styledLayout = new StyledLayout();
    private final StyledPaint styledPaint = new StyledPaint();
    private final VectorPaint vectorPaint = new VectorPaint();
    private final int boxSplit = 1;//九宫格占位比例
    private final int lineSplit = 2;//空白行占位比例
    private final int lineSpace;//行间距
    private boolean drawSimpleTextBySystem = true;//是否使用系统方式（TextView）绘制简单文本
    private int leftSpace;//默认左部字幕避让位置
    private int topSpace;//默认顶部字幕避让位置
    private int rightSpace;//默认右部字幕避让位置
    private int bottomSpace;//默认底部字幕避让位置
    private float aspectRatio = 0;//视频宽高比
    private float srtBottomSpace = 60;//设置默认入幕srt底部距离
    private int width, height;//绘制宽高
    private float fontScale = 1f;
    private boolean drawPath = true;//绘制path

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
        Map<Integer, RenderedModel> models = render.render();
        if (models != null) {
            showModels.putAll(models);
        }
        styledPaint.setPtSize(render.playResX, render.playResY);
        vectorPaint.setPtSize(render.playResX, render.playResY);
    }

    /**
     * 绘制字幕
     */
    public void render(@NonNull Canvas canvas) {
        Rect r = canvas.getClipBounds();
        this.width = r.width();
        this.height = r.height();
        if (aspectRatio == 0) {
            if (this.height != 0) {
                aspectRatio = this.width * 1f / this.height;
            } else {
                return;
            }
        }
        Rect area = getDrawArea(null);
        styledPaint.setScreenSize(area.width(), area.height());
        vectorPaint.setScreenSize(area.width(), area.height());
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
            styledPaint.setFontScale(fontScale);
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
                    float y = height - styledPaint.descent() - ((lines.size() - 1 - i) * measureHeight);
                    y -= i * lineSpace;
                    styledPaint.drawText(canvas, text, x, y, null);
                }
            }
            styledPaint.setFontScale(1f);
            return;
        }
        if (showModels.isEmpty()) {
            return;
        }
        RenderedModel un = showModels.get(SubtitlesDecoder.GRAVITY_UNSET);
        if (un != null) {
            for (List<SubtitlesModel> model : un.getModels()) {
                drawAbs(model, canvas);
            }
        }

        styledPaint.setFontScale(fontScale);
        RenderedModel an1 = showModels.get(SubtitlesDecoder.GRAVITY_AN1);
        if (an1 != null) {
            for (List<SubtitlesModel> model : an1.getModels()) {
                drawBottom(model, canvas, Gravity.LEFT);
            }
        }
        RenderedModel an2 = showModels.get(SubtitlesDecoder.GRAVITY_AN2);
        if (an2 != null) {
            for (List<SubtitlesModel> model : an2.getModels()) {
                drawBottom(model, canvas, Gravity.CENTER);
            }
        }
        RenderedModel an3 = showModels.get(SubtitlesDecoder.GRAVITY_AN3);
        if (an3 != null) {
            for (List<SubtitlesModel> model : an3.getModels()) {
                drawBottom(model, canvas, Gravity.RIGHT);
            }
        }
        styledPaint.setFontScale(1f);

        RenderedModel an4 = showModels.get(SubtitlesDecoder.GRAVITY_AN4);
        if (an4 != null) {
            for (List<SubtitlesModel> model : an4.getModels()) {
                drawAn(model, canvas, Gravity.LEFT, Gravity.CENTER);
            }
        }
        RenderedModel an5 = showModels.get(SubtitlesDecoder.GRAVITY_AN5);
        if (an5 != null) {
            for (List<SubtitlesModel> model : an5.getModels()) {
                drawAn(model, canvas, Gravity.CENTER, Gravity.CENTER);
            }
        }
        RenderedModel an6 = showModels.get(SubtitlesDecoder.GRAVITY_AN6);
        if (an6 != null) {
            for (List<SubtitlesModel> model : an6.getModels()) {
                drawAn(model, canvas, Gravity.RIGHT, Gravity.CENTER);
            }
        }

        RenderedModel an7 = showModels.get(SubtitlesDecoder.GRAVITY_AN7);
        if (an7 != null) {
            for (List<SubtitlesModel> model : an7.getModels()) {
                drawAn(model, canvas, Gravity.LEFT, Gravity.TOP);
            }
        }
        RenderedModel an8 = showModels.get(SubtitlesDecoder.GRAVITY_AN8);
        if (an8 != null) {
            for (List<SubtitlesModel> model : an8.getModels()) {
                drawAn(model, canvas, Gravity.CENTER, Gravity.TOP);
            }
        }
        RenderedModel an9 = showModels.get(SubtitlesDecoder.GRAVITY_AN9);
        if (an9 != null) {
            for (List<SubtitlesModel> model : an9.getModels()) {
                drawAn(model, canvas, Gravity.RIGHT, Gravity.TOP);
            }
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
                float savedFontScale = extra.currentFontScale;
                Rect area = getDrawArea(extra);
                if (extra.currentClip != null) {
                    //绘制区域不可见，不绘制
                    if (extra.currentClip.left == extra.currentClip.right || extra.currentClip.top == extra.currentClip.bottom) {
                        continue;
                    }
                    //注意：extra是复用对象，多线程执行render可能将其渲染回1
                    //字高根据实际视频高度计算，当前视频使用定高宽，绘制字体偏大，缩小避免绘制超出clip区域
                    //竖屏全屏宽高改变，计算异常
//                  extra.currentFontScale = extra.currentFontScale * Math.min(1f, (width * 1f / height) / aspectRatio);
                }
                if (extra.isPath) {
                    if (drawPath) {
                        vectorPaint.setArea(area);
                        vectorPaint.drawVector(subtitlesModel, canvas);
                    }
                    continue;
                }
                styledPaint.setMode(StyledPaint.MODE_BODY);
                styledPaint.setPaint(extra);
                float x = extra.x * area.width() + area.left;
//                float x = extra.x * getWidth();
                List<CharSequence> lines = styledLayout.pageLines(subtitlesModel.text(), styledPaint, extra, width, 1);
                float y = extra.y * area.height() + area.top;
                float totalHeight = measureHeight(lines, extra);
                //字幕行的行对齐方式决定了位置设定的参考点。
                //举例来说，当行对齐设定为左上时，字幕行的左上角会被放置在 \pos 指定的位置，对于底部中间对齐来说，字幕的底部中间位置将会被放置在指定的坐标上。
                float offsetY;
                if (extra.gravity == SubtitlesDecoder.GRAVITY_AN7 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
                    //顶部
                    offsetY = 0;
                } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN1 || extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN3) {
                    //底部
                    offsetY = totalHeight;
                } else if (extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
                    offsetY = 0;
                } else {
                    //中部
                    offsetY = totalHeight / 2;
                }
                if (extra.currentScaleX != 1) {
                    styledPaint.getTextPaint().setTextScaleX(extra.currentScaleX);
                } else {
                    styledPaint.getTextPaint().setTextScaleX(1);
                }
                float[] xs = new float[lines.size()];
                float rangeLeft = -1;
                float rangeRight = -1;
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
                    xs[i] = finalX;
                    if (rangeLeft == -1) {
                        rangeLeft = finalX;
                    } else if (finalX < rangeLeft) {
                        rangeLeft = finalX;
                    }
                    if (rangeRight == -1) {
                        rangeRight = finalX + measureWidth;
                    } else if (finalX + measureWidth > rangeRight) {
                        rangeRight = finalX + measureWidth;
                    }
                }
                int count = updateCanvas(canvas, area, lines, rangeLeft, y - offsetY, rangeRight, y - offsetY + totalHeight, extra);
                for (int i = 0; i < lines.size(); i++) {
                    CharSequence text = lines.get(i);
                    if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                        y += styledPaint.measureHeight(text, extra) / lineSplit + lineSpace;
                    } else {
                        float lineHeight = styledPaint.measureHeight(text, extra);
                        y += lineHeight;
                        styledPaint.drawText(canvas, text, xs[i], y - offsetY - styledPaint.descent(), extra);
                        y += lineSpace;
                    }
                }
                styledPaint.getTextPaint().setTextScaleX(1);
                extra.currentFontScale = savedFontScale;
                if (count != -1) {
                    try {
                        //投影仪抛出异常，投影仪degree为0.00时候save判断未进入
                        //这里判断（degree != 0）有概率进入，改为判断自定义变量
                        canvas.restoreToCount(count);
                    } catch (Exception e) {
                        Log.e("Canvas", "restore failed(" + extra.currentDegree + "," + count + "):" + subtitlesModel.content + "\n", e);
                    }
                }
            }
        }
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
            float left = getLeftSpace(extra);
            float right = getRightSpace(extra);
            List<CharSequence> lines = pageLines(subtitlesModel.text(), extra, (int) (width - left - right), xGravity == Gravity.CENTER ? 1 : boxSplit);
            float totalHeight = measureHeight(lines, extra);
            if (bottom == -1) {
                if (yGravity == Gravity.CENTER) {
                    bottom = (height - totalHeight) / 2;
                } else {
                    bottom = getTopSpace(extra);
                }
            }
            float[] xs = new float[lines.size()];
            float rangeLeft = -1;
            float rangeRight = -1;
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float measureWidth = styledPaint.measureText(text, 0, text.length(), extra);
                float x = (width - measureWidth) / 2;
                if (xGravity == Gravity.LEFT) {
                    x = left;
                } else if (xGravity == Gravity.RIGHT) {
                    x = width - measureWidth - right;
                } else {
                    x += (left - right) / 2;
                }
                xs[i] = x;
                if (rangeLeft == -1) {
                    rangeLeft = x;
                } else if (x < rangeLeft) {
                    rangeLeft = x;
                }
                if (rangeRight == -1) {
                    rangeRight = x + measureWidth;
                } else if (x + measureWidth > rangeRight) {
                    rangeRight = x + measureWidth;
                }
            }
            Rect area = getDrawArea(extra);
            int count = updateCanvas(canvas, area, lines, rangeLeft, bottom, rangeRight, bottom + totalHeight, extra);
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float lineHeight = styledPaint.measureHeight(text, extra);
                if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                    bottom += lineHeight / lineSplit;
                } else {
                    bottom += lineHeight;
                    styledPaint.drawText(canvas, text, xs[i], bottom - styledPaint.descent(), extra);
                }
                bottom += lineSpace;
            }
            if (count != -1) {
                canvas.restoreToCount(count);
            }
        }
    }

    /**
     * 绘制底部文字an1、an2、an3，倒序绘制
     */
    private void drawBottom(List<SubtitlesModel> subtitlesModels, @NonNull Canvas canvas, int gravity) {
        float bottom = -1;
        boolean checkSpace = mayDiffMarginH(subtitlesModels);
        List<RangeH> ranges = null;
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
            float left = getLeftSpace(extra);
            float right = getRightSpace(extra);
            List<CharSequence> lines = pageLines(subtitlesModel.text(), extra, (int) (width - left - right), gravity == Gravity.CENTER ? 1 : boxSplit);
            float[] xs = new float[lines.size()];
            float rangeLeft = -1;
            float rangeRight = -1;
            //计算各行绘制x坐标起点
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float measureWidth = styledPaint.measureText(text, 0, text.length(), extra);
                float x = (width - measureWidth) / 2;
                if (gravity == Gravity.LEFT) {
                    x = left;
                } else if (gravity == Gravity.RIGHT) {
                    x = width - measureWidth - right;
                } else {
                    x += (left - right) / 2;
                }
                xs[i] = x;
                if (rangeLeft == -1) {
                    rangeLeft = x;
                } else if (x < rangeLeft) {
                    rangeLeft = x;
                }
                if (rangeRight == -1) {
                    rangeRight = x + measureWidth;
                } else if (x + measureWidth > rangeRight) {
                    rangeRight = x + measureWidth;
                }
            }
            RangeH range = null;
            if (checkSpace) {
                if (ranges == null) {
                    ranges = new ArrayList<>();
                }
                range = findRange(ranges, rangeLeft, rangeRight, extra);
                bottom = range.bottom;
            }
            float totalHeight = measureHeight(lines, extra);
            bottom -= totalHeight;
            if (range != null) {
                range.bottom = bottom;
            }
            Rect area = getDrawArea(extra);
            int count = updateCanvas(canvas, area, lines, rangeLeft, bottom, rangeRight, bottom + totalHeight, extra);
            float itemBottom = bottom;
            for (int i = 0; i < lines.size(); i++) {
                CharSequence text = lines.get(i);
                float lineHeight = styledPaint.measureHeight(text, extra);
                if (TextUtils.isEmpty(text) || "\n".contentEquals(text)) {
                    itemBottom += lineHeight / lineSplit;
                } else {
                    itemBottom += lineHeight;
                    styledPaint.drawText(canvas, text, xs[i], itemBottom - styledPaint.descent(), extra);
                }
                itemBottom += lineSpace;
            }
            if (count != -1) {
                canvas.restoreToCount(count);
            }
        }
    }

    /**
     * 画布变化相关标签
     */
    private int updateCanvas(Canvas canvas, Rect area, List<CharSequence> lines, float left, float top, float right, float bottom, Style extra) {
        int count = -1;
        if (extra == null) {
            return count;
        }

        if (extra.currentClip == null && extra.currentDegree == 0 && extra.currentDegreeX == 0 && extra.currentDegreeY == 0) {
            return count;
        }

        float x;
        float y;
        //an7 an8 an9
        //an4 an5 an6
        //an1 an2 an3
        //旋转中心点基于an
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
            x = (right - left) / 2 + left;
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN3 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            x = right;
        } else {
            x = left;
        }
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN4 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
            y = (bottom - top) / 2 + top;
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN7 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            y = top;
        } else {
            y = bottom;
        }

        if (extra.currentClip != null) {
            Rect clip = clipRect(extra, area, lines.size());
            count = canvas.save();
            canvas.clipRect(clip);
        }
        if (extra.currentDegree != 0) {
            if (count == -1) {
                count = canvas.save();
            }
            float degree = -extra.currentDegree;
            canvas.rotate(degree, x, y);
        }
        if (extra.currentDegreeX != 0 || extra.currentDegreeY != 0) {
            if (count == -1) {
                count = canvas.save();
            }
            float xd = (float) Math.cos(Math.toRadians(extra.currentDegreeY));
            float yd = (float) Math.cos(Math.toRadians(extra.currentDegreeX));
            canvas.scale(xd, yd, x, y);
        }
        if (extra.getFaX() != 0 || extra.getFaY() != 0) {
            if (count == -1) {
                count = canvas.save();
            }
            canvas.translate(-x * extra.getFaX(), -y * extra.getFaY());
            canvas.skew(extra.getFaX(), extra.getFaY());
        }
        return count;
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
     * 存在不同横向margin。可能需要跌落
     */
    private boolean mayDiffMarginH(List<SubtitlesModel> subtitlesModels) {
        if (subtitlesModels.size() < 2) {
            return false;
        }
        SubtitlesModel model = subtitlesModels.get(0);
        if (model.style == null) {
            return false;
        }
        float l = model.style.getMarginL();
        float r = model.style.getMarginR();
        for (int i = 1; i < subtitlesModels.size(); i++) {
            model = subtitlesModels.get(i);
            if (model.style == null) {
                return false;
            }
            if (model.style.getMarginL() != l || model.style.getMarginR() != r) {
                return true;
            }
        }
        return false;
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
        if (style == null) {
            return leftSpace;
        }
        Rect area = getDrawArea(style);
        float scale = style.playResX == 0 ? 1 : area.width() * 1f / style.playResX;
        return area.left + style.getMarginL() * scale;
    }

    private float getTopSpace(Style style) {
        if (style == null) {
            return topSpace;
        }
        Rect area = getDrawArea(style);
        float scale = style.playResY == 0 ? 1 : area.height() * 1f / style.playResY;
        return area.top + style.getMarginV() * scale;
    }

    private float getRightSpace(Style style) {
        if (style == null) {
            return rightSpace;
        }
        Rect area = getDrawArea(style);
        float scale = style.playResX == 0 ? 1 : area.width() * 1f / style.playResX;
        return width - area.right + style.getMarginR() * scale;
    }

    private float getBottomSpace(Style style) {
        Rect area = getDrawArea(style);
        //srt无底部边距定义，视频内显示使用固定边距
        if (style == null || !style.hasMarginV()) {
            return height - area.bottom + (srtBottomSpace * (height / 1080f));
        }
        float scale = style.playResY == 0 ? 1 : area.height() * 1f / style.playResY;
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
        if (aspectRatio != 0) {
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
            if (style == null) {
                rect.right = width;
                rect.bottom = height;
            } else {
                float aspectRatio = style.playResX * 1f / style.playResY;
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
            }
        }
        return rect;
    }

    private List<CharSequence> pageLines(CharSequence text, Style style, int split) {
        return pageLines(text, style, width, split);
    }

    private List<CharSequence> pageLines(CharSequence text, Style style, int width, int split) {
        return styledLayout.pageLines(text, styledPaint, style, width, split);
    }

    /**
     * 设置是否使用系统方式（TextView）绘制简单文本
     */
    public void setDrawSimpleTextBySystem(boolean drawSimpleTextBySystem) {
        this.drawSimpleTextBySystem = drawSimpleTextBySystem;
    }

    /**
     * 设置字体缩放，仅对底部字幕生效
     */
    public void setFontScale(float scale) {
        this.fontScale = scale;
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
    public void setAspectRatio(int videoWidth, int videoHeight) {
        this.aspectRatio = videoWidth * 1f / videoHeight;
        this.styledPaint.setVideoSize(videoWidth, videoHeight);
        this.vectorPaint.setVideoSize(videoWidth, videoHeight);
    }

    /**
     * 设置入幕srt底部距离
     */
    public void setSrtBottomSpace(float srtBottomSpace) {
        this.srtBottomSpace = srtBottomSpace;
    }

    /**
     * 设置边框缩放
     */
    public void setStrokeScale(int strokeScale) {
        this.styledPaint.setStrokeScale(strokeScale);
    }

    /**
     * 设置边框圆角化
     */
    public void setRoundStroke(boolean roundStroke) {
        this.styledPaint.setRoundStroke(roundStroke);
    }

    /**
     * 设置绘制path
     */
    public void setDrawPath(boolean drawPath) {
        this.drawPath = drawPath;
    }

    /**
     * 字幕横向显示范围
     */
    private static class RangeH {
        float left;
        float right;
        float bottom;

        void update(float left, float right) {
            if (left < this.left) {
                this.left = left;
            }
            if (right > this.right) {
                this.right = right;
            }
        }
    }

    /**
     * 查找当前横向显示范围
     */
    @NonNull
    private RangeH findRange(List<RangeH> ranges, float rangeLeft, float rangeRight, Style extra) {
        if (!ranges.isEmpty()) {
            for (RangeH range : ranges) {
                float mLeft = range.left;
                float mRight = range.right;
                if (rangeLeft > mLeft && rangeLeft < mRight) {
                    range.update(rangeLeft, rangeRight);
                    return range;
                }
                if (rangeLeft < mLeft) {
                    if (rangeRight > mLeft) {
                        range.update(rangeLeft, rangeRight);
                        return range;
                    }
                }
            }
        }
        RangeH range = new RangeH();
        range.left = rangeLeft;
        range.right = rangeRight;
        int shadow = 3;
        if (extra != null) {
            shadow += (int) extra.getShadowWidth();
        }
        range.bottom = height - getBottomSpace(extra) - shadow;
        ranges.add(range);
        return range;
    }
}
