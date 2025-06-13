package com.loror.subtitle.model;

import android.graphics.Color;
import android.graphics.Rect;

import com.loror.subtitle.SubtitlesDecoder;
import com.loror.subtitle.render.Style;
import com.loror.subtitle.util.SubColorUtil;

import java.util.List;

public class SubtitlesModel {

    /**
     * 当前节点
     */
    public int node;

    /**
     * 当前排序
     */
    public int sort = -1;

    /**
     * 0 srt 1 ass
     */
    public int type;

    /**
     * 开始显示的时间
     */
    public int star;

    /**
     * 结束显示的时间
     */
    public int end;

    /**
     * 图层
     */
    public int layer;

    /**
     * 绝对定位基准
     */
    public int playResX;

    /**
     * 绝对定位基准
     */
    public int playResY;

    /**
     * 字幕其他信息
     */
    public String extra;

    /**
     * 显示的内容
     */
    public CharSequence content;
    protected CharSequence renderText;

    /**
     * 继承样式名称
     */
    public String styleName;

    /**
     * 样式解析结果
     */
    public Style style;

    public void setRenderText(CharSequence text) {
        renderText = text;
    }

    public CharSequence text() {
        if (content == null) {
            return "";
        }
        if (renderText != null) {
            return renderText;
        }
        return content;
    }

    public SubtitlesModel copy() {
        SubtitlesModel model = new SubtitlesModel();
        model.node = node;
        model.sort = sort;
        model.star = star;
        model.end = end;
        model.extra = extra;
        model.playResX = playResX;
        model.playResY = playResY;
        model.content = content;
        model.styleName = styleName;
        model.style = style;
        return model;
    }

    private List<Path> paths;

    public List<Path> getPaths() {
        if (paths == null) {
            paths = SubtitlesDecoder.Extra.parsePath(this);
        }
        return paths;
    }

    @Override
    public String toString() {
        return "SubtitlesModel{" +
                "node=" + node +
                ", sort=" + sort +
                ", type=" + type +
                ", star=" + star +
                ", end=" + end +
                ", layer=" + layer +
                ", content='" + content + '\'' +
                ", extra='" + extra + '\'' +
                ", styleName='" + styleName + '\'' +
                ", style='" + style + '\'' +
                '}';
    }

    public static class SubtitlesStyle {

        public int playResX;//字幕标定屏幕大小
        public int playResY;//字幕标定屏幕大小
        public int gravity;//9宫格位置
        private String font;//字体
        private Boolean fontFall;//字体向左倾倒
        /**
         * ass的字体的fontsize的单位是pt（磅）
         * 1px（像素）=(72/dpi)*pt
         * win默认dpi是96
         */
        private Integer fontSize;
        private Integer scaleX;
        private Integer scaleY;
        private Integer fontAlpha;//字体透明度
        private Integer fontColor;//字体颜色
        private Integer fontSecondaryAlpha;//字体次要颜色透明度
        private Integer fontSecondaryColor;//字体次要颜色
        private Integer borderAlpha;//边框颜色透明度
        private Integer borderColor;//边框颜色
        private Integer shadowAlpha;//阴影颜色透明度
        private Integer shadowColor;//阴影颜色
        private Float borderWidth;//边框大小
        private Float shadowWidth;//阴影大小
        private Integer borderStyle;//边框样式
        private Float space;//文字间距
        private Boolean isBold;//加粗
        private Boolean isItalic;//倾斜
        private Boolean isUnderline;//下划线
        private Boolean isStrikeout;//中划线
        private Float degree;//旋转角度（逆时针）
        private Float degreeX;//旋转角度（x轴）
        private Float degreeY;//旋转角度（y轴）
        private Float faX;//斜切（x轴）
        private Float faY;//斜切（y轴）
        private Float positionX;//相对屏幕坐标
        private Float positionY;//相对屏幕坐标
        private Rect clip;//裁剪
        private Rect iClip;//裁剪
        private Float blur;//高斯模糊
        private Integer be;//边框高斯模糊
        private Float marginL;//左边距
        private Float marginR;//右边距
        private Float marginV;//垂直边距
        private Integer wrap;//换行模式
        public boolean isPath;//是否是矢量图
        public int pathScale;//矢量图坐标缩放
        private int durationT1;//t动画时间暂存
        private int durationT2;//t动画时间暂存
        public int durationK;//卡拉ok时间
        private SubtitlesAnimation animation;//动画

        public SubtitlesStyle() {

        }

        public SubtitlesStyle(SubtitlesStyle style) {
            if (style == null) {
                return;
            }
            this.gravity = style.gravity;
            this.font = style.font;
            this.fontFall = style.fontFall;
            this.fontSize = style.fontSize;
            this.scaleX = style.scaleX;
            this.scaleY = style.scaleY;
            this.fontAlpha = style.fontAlpha;
            this.fontColor = style.fontColor;
            this.fontSecondaryAlpha = style.fontSecondaryAlpha;
            this.fontSecondaryColor = style.fontSecondaryColor;
            this.borderAlpha = style.borderAlpha;
            this.borderColor = style.borderColor;
            this.shadowAlpha = style.shadowAlpha;
            this.shadowColor = style.shadowColor;
            this.borderWidth = style.borderWidth;
            this.shadowWidth = style.shadowWidth;
            this.borderStyle = style.borderStyle;
            this.space = style.space;
            this.isBold = style.isBold;
            this.isItalic = style.isItalic;
            this.isUnderline = style.isUnderline;
            this.isStrikeout = style.isStrikeout;
            this.degree = style.degree;
            this.degreeX = style.degreeX;
            this.degreeY = style.degreeY;
            this.faX = style.faX;
            this.faY = style.faY;
            this.positionX = style.positionX;
            this.positionY = style.positionY;
            this.clip = style.clip;
            this.blur = style.blur;
            this.be = style.be;
            this.marginL = style.marginL;
            this.marginR = style.marginR;
            this.marginV = style.marginV;
            this.wrap = style.wrap;
            this.durationT1 = style.durationT1;
            this.durationT2 = style.durationT2;
            this.durationK = style.durationK;
            this.playResX = style.playResX;
            this.playResY = style.playResY;
            this.isPath = style.isPath;
            this.pathScale = style.pathScale;
            this.animation = style.animation;
        }

        public void extendStyle(SubtitlesStyle value) {
            if (value == null) {
                return;
            }
            if (value.gravity != SubtitlesDecoder.GRAVITY_UNSET) {
                this.gravity = value.gravity;
            }
            if (value.font != null) {
                this.font = value.font;
            }
            if (value.fontFall != null) {
                this.fontFall = value.fontFall;
            }
            if (value.fontSize != null) {
                this.fontSize = value.fontSize;
            }
            if (value.scaleX != null) {
                this.scaleX = value.scaleX;
            }
            if (value.scaleY != null) {
                this.scaleY = value.scaleY;
            }
            if (value.fontAlpha != null) {
                this.fontAlpha = value.fontAlpha;
            }
            if (value.fontColor != null) {
                this.fontColor = value.fontColor;
            }
            if (value.fontSecondaryAlpha != null) {
                this.fontSecondaryAlpha = value.fontSecondaryAlpha;
            }
            if (value.fontSecondaryColor != null) {
                this.fontSecondaryColor = value.fontSecondaryColor;
            }
            if (value.borderAlpha != null) {
                this.borderAlpha = value.borderAlpha;
            }
            if (value.borderColor != null) {
                this.borderColor = value.borderColor;
            }
            if (value.shadowAlpha != null) {
                this.shadowAlpha = value.shadowAlpha;
            }
            if (value.shadowColor != null) {
                this.shadowColor = value.shadowColor;
            }
            if (value.borderWidth != null) {
                this.borderWidth = value.borderWidth;
            }
            if (value.shadowWidth != null) {
                this.shadowWidth = value.shadowWidth;
            }
            if (value.borderStyle != null) {
                this.borderStyle = value.borderStyle;
            }
            if (value.space != null) {
                this.space = value.space;
            }
            if (this.playResX == 0) {
                this.playResX = value.playResX;
            }
            if (this.playResY == 0) {
                this.playResY = value.playResY;
            }
            if (value.isBold != null) {
                this.isBold = value.isBold;
            }
            if (value.isItalic != null) {
                this.isItalic = value.isItalic;
            }
            if (value.isUnderline != null) {
                this.isUnderline = value.isUnderline;
            }
            if (value.isStrikeout != null) {
                this.isStrikeout = value.isStrikeout;
            }
            if (value.positionX != null) {
                this.positionX = value.positionX;
            }
            if (value.positionY != null) {
                this.positionY = value.positionY;
            }
            if (value.clip != null) {
                this.clip = value.clip;
            }
            if (value.blur != null) {
                this.blur = value.blur;
            }
            if (value.be != null) {
                this.be = value.be;
            }
            if (value.marginL != null) {
                this.marginL = value.marginL;
            }
            if (value.marginR != null) {
                this.marginR = value.marginR;
            }
            if (value.marginV != null) {
                this.marginV = value.marginV;
            }
            if (value.wrap != null) {
                this.wrap = value.wrap;
            }
            if (value.durationT1 != 0) {
                this.durationT1 = value.durationT1;
            }
            if (value.durationT2 != 0) {
                this.durationT2 = value.durationT2;
            }
            if (value.durationK != 0) {
                this.durationK = value.durationK;
            }
            if (value.pathScale != 0) {
                this.pathScale = value.pathScale;
            }
            if (value.degree != null) {
                this.degree = value.degree;
            }
            if (value.degreeX != null) {
                this.degreeX = value.degreeX;
            }
            if (value.degreeY != null) {
                this.degreeY = value.degreeY;
            }
            if (value.faX != null) {
                this.faX = value.faX;
            }
            if (value.faY != null) {
                this.faY = value.faY;
            }
            if (value.animation != null) {
                this.animation = value.animation;
            }
            this.isPath = value.isPath;
        }

        public void setGravity(String align) {
            try {
                gravity = Integer.parseInt(align);
            } catch (Exception e) {
                System.err.println("error parse align:" + align);
            }
        }

        public String getFont() {
            return font;
        }

        public void setFont(String font) {
            if (font != null && font.startsWith("@")) {
                this.fontFall = true;
                this.font = font.substring(1);
            } else {
                this.fontFall = false;
                this.font = font;
            }
        }

        public boolean hasFontSize() {
            return fontSize != null;
        }

        public boolean isFontFall() {
            return fontFall != null && fontFall;
        }

        public int getFontSize() {
            return fontSize == null ? 0 : fontSize;
        }

        public void setFontSize(String size) {
            try {
                fontSize = Math.round(Float.parseFloat(size));
            } catch (Exception e) {
                System.err.println("error parse fs:" + size);
            }
        }

        public float getScale() {
            if (scaleX == null || scaleY == null) {
                return 1f;
            }
            float min = Math.min(scaleX, scaleY);
            if (min <= 0) {
                min = 1f;
            } else {
                min = min / 100;
            }
            return min;
        }

        public boolean hasScaleX() {
            return scaleX != null;
        }

        public float getScaleX() {
            return scaleX == null ? 1 : (scaleX / 100f);
        }

        public void setScaleX(String size) {
            try {
                scaleX = (int) Float.parseFloat(size);
            } catch (Exception e) {
                System.err.println("error parse scaleX:" + size);
            }
        }

        public boolean hasScaleY() {
            return scaleY != null;
        }

        public float getScaleY() {
            return scaleY == null ? 1 : (scaleY / 100f);
        }

        public void setScaleY(String size) {
            try {
                scaleY = (int) Float.parseFloat(size);
            } catch (Exception e) {
                System.err.println("error parse scaleY:" + size);
            }
        }

        public boolean hasFontColor() {
            return fontColor != null;
        }

        public int getFontColor() {
            if (fontColor == null) {
                return Color.WHITE;
            }
            return fontAlpha == null ? fontColor : SubColorUtil.replaceColorAlpha(fontColor, fontAlpha);
        }

        public void setFontAlpha(String alpha) {
            try {
                this.fontAlpha = 255 - (int) Long.parseLong(alpha, 16);
            } catch (Exception e) {
                System.err.println("error parse fontAlpha:" + alpha);
            }
        }

        public void setFontColor(String color) {
            try {
                this.fontColor = SubColorUtil.parseBgr(color);
            } catch (Exception e) {
                System.err.println("error parse fontColor:" + color);
            }
        }

        /**
         * argb
         */
        public void setFontColor(int color) {
            this.fontColor = color;
        }

        public boolean hasFontSecondaryColor() {
            return fontSecondaryColor != null;
        }

        public int getFontSecondaryColor() {
            if (fontSecondaryColor == null) {
                return Color.WHITE;
            }
            return fontSecondaryAlpha == null ? fontSecondaryColor : SubColorUtil.replaceColorAlpha(fontSecondaryColor, fontSecondaryAlpha);
        }

        public void setFontSecondaryAlpha(String alpha) {
            try {
                this.fontSecondaryAlpha = 255 - (int) Long.parseLong(alpha, 16);
            } catch (Exception e) {
                System.err.println("error parse fontSecondaryAlpha:" + alpha);
            }
        }

        public void setFontSecondaryColor(String color) {
            try {
                this.fontSecondaryColor = SubColorUtil.parseBgr(color);
            } catch (Exception e) {
                System.err.println("error parse fontSecondaryColor:" + color);
            }
        }

        public boolean hasBorderColor() {
            return borderColor != null;
        }

        public int getBorderColor() {
            if (borderColor == null) {
                return Color.BLACK;
            }
            return borderAlpha == null ? borderColor : SubColorUtil.replaceColorAlpha(borderColor, borderAlpha);
        }

        public void setBorderAlpha(String alpha) {
            try {
                this.borderAlpha = 255 - (int) Long.parseLong(alpha, 16);
            } catch (Exception e) {
                System.err.println("error parse borderAlpha:" + alpha);
            }
        }

        public void setBorderColor(String color) {
            try {
                this.borderColor = SubColorUtil.parseBgr(color);
            } catch (Exception e) {
                System.err.println("error parse borderColor:" + color);
            }
        }

        public boolean hasShadowColor() {
            return shadowColor != null;
        }

        public int getShadowColor() {
            if (shadowColor == null) {
                return Color.BLACK;
            }
            return shadowAlpha == null ? shadowColor : SubColorUtil.replaceColorAlpha(shadowColor, shadowAlpha);
        }

        public void setShadowAlpha(String alpha) {
            try {
                this.shadowAlpha = 255 - (int) Long.parseLong(alpha, 16);
            } catch (Exception e) {
                System.err.println("error parse shadowAlpha:" + alpha);
            }
        }

        public void setShadowColor(String color) {
            try {
                this.shadowColor = SubColorUtil.parseBgr(color);
            } catch (Exception e) {
                System.err.println("error parse shadowColor:" + color);
            }
        }

        public boolean hasBorderWidth() {
            return borderWidth != null;
        }

        public float getBorderWidth() {
            return borderWidth == null ? 2f : borderWidth;
        }

        public void setBorderWidth(String size) {
            try {
                borderWidth = Float.parseFloat(size);
            } catch (Exception e) {
                System.err.println("error parse borderWidth:" + size);
            }
        }

        public boolean hasShadowWidth() {
            return shadowWidth != null;
        }

        public float getShadowWidth() {
            return shadowWidth == null ? 1 : shadowWidth;
        }

        public void setShadowWidth(String size) {
            try {
                shadowWidth = Float.parseFloat(size);
            } catch (Exception e) {
                System.err.println("error parse shadowWidth:" + size);
            }
        }

        public boolean hasBold() {
            return isBold != null;
        }

        public boolean isBold() {
            return Boolean.TRUE.equals(isBold);
        }

        public void setBold(String bold) {
            try {
                int weight = Integer.parseInt(bold);
                isBold = weight == -1 || weight == 1 || weight > 700;
            } catch (Exception e) {
                System.err.println("error parse isBold:" + bold);
            }
        }

        public boolean hasItalic() {
            return isItalic != null;
        }

        public boolean isItalic() {
            return Boolean.TRUE.equals(isItalic);
        }

        public void setItalic(String italic) {
            try {
                isItalic = Math.abs(Integer.parseInt(italic)) == 1;
            } catch (Exception e) {
                System.err.println("error parse isItalic:" + italic);
            }
        }

        public boolean hasUnderline() {
            return isUnderline != null;
        }

        public boolean isUnderline() {
            return Boolean.TRUE.equals(isUnderline);
        }

        public void setUnderline(String underline) {
            try {
                isUnderline = Math.abs(Integer.parseInt(underline)) == 1;
            } catch (Exception e) {
                System.err.println("error parse isUnderline:" + underline);
            }
        }

        public boolean hasStrikeout() {
            return isStrikeout != null;
        }

        public boolean isStrikeout() {
            return Boolean.TRUE.equals(isStrikeout);
        }

        public void setStrikeout(String strikeout) {
            try {
                isStrikeout = Math.abs(Integer.parseInt(strikeout)) == 1;
            } catch (Exception e) {
                System.err.println("error parse strikeout:" + strikeout);
            }
        }

        public int getBorderStyle() {
            return borderStyle == null ? 1 : borderStyle;
        }

        public void setBorderStyle(String borderStyle) {
            try {
                this.borderStyle = Integer.parseInt(borderStyle);
            } catch (Exception e) {
                System.err.println("error parse borderStyle:" + borderStyle);
            }
        }

        public boolean hasSpace() {
            return space != null;
        }

        public float getSpace() {
            return space == null ? 0 : space;
        }

        public void setSpace(String space) {
            try {
                this.space = Float.parseFloat(space);
            } catch (Exception e) {
                System.err.println("error parse space:" + space);
            }
        }

        public boolean hasDegree() {
            return degree != null;
        }

        public float getDegree() {
            return degree == null ? 0 : degree;
        }

        public void setDegree(String degree) {
            try {
                this.degree = Float.parseFloat(degree);
            } catch (Exception e) {
                System.err.println("error parse degree:" + degree);
            }
        }

        public float getDegreeX() {
            return degreeX == null ? 0 : degreeX;
        }

        public void setDegreeX(String degree) {
            try {
                this.degreeX = Float.parseFloat(degree);
            } catch (Exception e) {
                System.err.println("error parse degree x:" + degree);
            }
        }

        public float getDegreeY() {
            return degreeY == null ? 0 : degreeY;
        }

        public void setDegreeY(String degree) {
            try {
                this.degreeY = Float.parseFloat(degree);
            } catch (Exception e) {
                System.err.println("error parse degree y:" + degree);
            }
        }

        public float getFaX() {
            return faX == null ? 0 : faX;
        }

        public void setFaX(String degree) {
            try {
                this.faX = Float.parseFloat(degree);
            } catch (Exception e) {
                System.err.println("error parse fay x:" + degree);
            }
        }

        public float getFaY() {
            return faY == null ? 0 : faY;
        }

        public void setFaY(String degree) {
            try {
                this.faY = Float.parseFloat(degree);
            } catch (Exception e) {
                System.err.println("error parse fay y:" + degree);
            }
        }

        public boolean hasPositionX() {
            return positionX != null;
        }

        public float getPositionX() {
            return positionX == null ? 0 : positionX;
        }

        public boolean hasPositionY() {
            return positionY != null;
        }

        public float getPositionY() {
            return positionY == null ? 0 : positionY;
        }

        public void setPosition(String position) {
            try {
                String[] location = position.substring(1).split(",");
                if (location.length == 2) {
                    try {
                        positionX = Float.parseFloat(location[0].trim());
                        positionY = Float.parseFloat(location[1].trim());
                    } catch (Exception e) {
                        System.err.println("error parse position:" + position);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse position:" + position);
            }
        }

        public void setMove(String move) {
            try {
                //遇到move携带空格，暂单独位置标签trim
                String[] location = move.substring(1).split(",");
                if (location.length >= 4) {
                    try {
                        SubtitlesAnimation.MoveAnimation animation = new SubtitlesAnimation.MoveAnimation();
                        animation.fromX = Float.parseFloat(location[0].trim());
                        animation.fromY = Float.parseFloat(location[1].trim());
                        animation.toX = Float.parseFloat(location[2].trim());
                        animation.toY = Float.parseFloat(location[3].trim());
                        if (location.length > 4) {
                            animation.durationStart = Integer.parseInt(location[4].trim());
                        } else {
                            animation.durationStart = -1;
                        }
                        if (location.length > 5) {
                            animation.durationEnd = Integer.parseInt(location[5].trim());
                        } else {
                            animation.durationEnd = -1;
                        }
                        addAnimation(animation);
                    } catch (Exception e) {
                        System.err.println("error parse move:" + move);
                    }
                } else if (location.length >= 2) {
                    try {
                        positionX = Float.parseFloat(location[0].trim());
                        positionY = Float.parseFloat(location[1].trim());
                    } catch (Exception e) {
                        System.err.println("error parse move:" + move);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse move:" + move);
            }
        }

        public void setT(String t) {
            try {
                String[] location = t.substring(1).split(",");
                if (location.length >= 2) {
                    if (location[0].startsWith("\\")) {
                        durationT1 = -1;
                        durationT2 = -1;
                    } else {
                        durationT1 = (int) Float.parseFloat(location[0]);
                        durationT2 = (int) Float.parseFloat(location[1]);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse t:" + t);
            }
        }

        public Rect getClip() {
            return clip;
        }

        public void setClip(String clip) {
            try {
                String[] location = clip.substring(1).split(",");
                if (location.length == 4) {
                    try {
                        float x1 = Float.parseFloat(location[0]);
                        float y1 = Float.parseFloat(location[1]);
                        float x2 = Float.parseFloat(location[2]);
                        float y2 = Float.parseFloat(location[3]);
                        this.clip = new Rect(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
                    } catch (Exception e) {
                        System.err.println("error parse clip:" + clip);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse clip:" + clip);
            }
        }

        public void setIClip(String clip) {
            try {
                String[] location = clip.substring(1).split(",");
                if (location.length == 4) {
                    try {
                        float x1 = Float.parseFloat(location[0]);
                        float y1 = Float.parseFloat(location[1]);
                        float x2 = Float.parseFloat(location[2]);
                        float y2 = Float.parseFloat(location[3]);
                        this.iClip = new Rect(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
                    } catch (Exception e) {
                        System.err.println("error parse iclip:" + clip);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse clip:" + clip);
            }
        }

        public Float getBlur() {
            return blur == null ? 0 : blur;
        }

        public boolean hasBlur() {
            return blur != null;
        }

        public void setBlur(String blur) {
            try {
                this.blur = Float.parseFloat(blur);
            } catch (NumberFormatException e) {
                System.err.println("error parse blur:" + blur);
            }
        }

        public int getBe() {
            return be == null ? 0 : be;
        }

        public boolean hasBe() {
            return be != null;
        }

        public void setBe(String be) {
            try {
                this.be = Integer.parseInt(be);
            } catch (NumberFormatException e) {
                System.err.println("error parse be:" + be);
            }
        }

        public Float getMarginL() {
            return marginL == null ? 0 : marginL;
        }

        public void setMarginL(String margin) {
            try {
                this.marginL = Float.parseFloat(margin);
            } catch (NumberFormatException e) {
                System.err.println("error parse marginL:" + margin);
            }
        }

        public Float getMarginR() {
            return marginR == null ? 0 : marginR;
        }

        public void setMarginR(String margin) {
            try {
                this.marginR = Float.parseFloat(margin);
            } catch (NumberFormatException e) {
                System.err.println("error parse marginR:" + margin);
            }
        }

        public Float getMarginV() {
            return marginV == null ? 0 : marginV;
        }

        public boolean hasMarginV() {
            return marginV != null;
        }

        public void setMarginV(String margin) {
            try {
                this.marginV = Float.parseFloat(margin);
            } catch (NumberFormatException e) {
                System.err.println("error parse marginV:" + margin);
            }
        }

        public Integer getWrap() {
            return wrap == null ? 0 : wrap;
        }

        public void setWrap(String wrap) {
            try {
                this.wrap = Integer.parseInt(wrap);
            } catch (NumberFormatException e) {
                System.err.println("error parse wrap:" + wrap);
            }
        }

        public void setWrap(Integer wrap) {
            this.wrap = wrap;
        }

        public void addClipAnimation(String clip) {
            try {
                String[] location = clip.substring(1).split(",");
                if (location.length == 4) {
                    try {
                        float x1 = Float.parseFloat(location[0]);
                        float y1 = Float.parseFloat(location[1]);
                        float x2 = Float.parseFloat(location[2]);
                        float y2 = Float.parseFloat(location[3]);
                        SubtitlesAnimation.ClipAnimation animation = new SubtitlesAnimation.ClipAnimation();
                        animation.toClip = new Rect(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2));
                        animation.durationStart = durationT1;
                        animation.durationEnd = durationT2;
                        addAnimation(animation);
                    } catch (Exception e) {
                        System.err.println("error parse to clip:" + clip);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse to clip:" + clip);
            }
        }

        public void addFontSizeAnimation(String size) {
            try {
                int fontSize = Math.round(Float.parseFloat(size));
                SubtitlesAnimation.FsAnimation animation = new SubtitlesAnimation.FsAnimation();
                animation.fontSize = fontSize;
                animation.durationStart = durationT1;
                animation.durationEnd = durationT2;
                addAnimation(animation);
            } catch (Exception e) {
                System.err.println("error parse fs anim:" + size);
            }
        }

        public void addDegreeAnimation(String type, String d) {
            try {
                float degree = Float.parseFloat(d);
                SubtitlesAnimation.DegreeAnimation animation = new SubtitlesAnimation.DegreeAnimation();
                animation.type = type;
                animation.degree = degree;
                animation.durationStart = durationT1;
                animation.durationEnd = durationT2;
                addAnimation(animation);
            } catch (Exception e) {
                System.err.println("error parse degree anima:" + d);
            }
        }

        public void addFsScaleAnimation(String type, String d) {
            try {
                float fs = Float.parseFloat(d);
                SubtitlesAnimation.FsScaleAnimation animation = new SubtitlesAnimation.FsScaleAnimation();
                animation.type = type;
                animation.scale = fs / 100;
                animation.durationStart = durationT1;
                animation.durationEnd = durationT2;
                addAnimation(animation);
            } catch (Exception e) {
                System.err.println("error parse degree anima:" + d);
            }
        }

        public void addBlurAnimation(String b) {
            try {
                float blur = Float.parseFloat(b);
                SubtitlesAnimation.BlurAnimation animation = new SubtitlesAnimation.BlurAnimation();
                animation.blur = blur;
                animation.durationStart = durationT1;
                animation.durationEnd = durationT2;
                addAnimation(animation);
            } catch (Exception e) {
                System.err.println("error parse blur anima:" + b);
            }
        }

        /**
         * k单位是0.01秒
         */
        public void setK(String k) {
            try {
                this.durationK = (int) (Float.parseFloat(k) * 0.01 * 1000);
            } catch (Exception e) {
                System.err.println("error parse k:" + k);
            }
        }

        public void setFad(String fade) {
            try {
                if (fade.charAt(0) == '(') {
                    String[] duration = fade.substring(1).split(",");
                    SubtitlesAnimation.FadeAnimation animation = new SubtitlesAnimation.FadeAnimation();
                    animation.durationStart = (int) Float.parseFloat(duration[0].trim());
                    animation.durationEnd = (int) Float.parseFloat(duration[1].trim());
                    addAnimation(animation);
                }
            } catch (Exception e) {
                System.err.println("error parse fad:" + fade);
            }
        }

        public void setFade(String fade) {
            try {
                if (fade.charAt(0) == '(') {
                    String[] fads = fade.substring(1).split(",");
                    if (fads.length == 7) {
                        SubtitlesAnimation.AlphaFadeAnimation animation = new SubtitlesAnimation.AlphaFadeAnimation();
                        animation.fromAlpha = (int) Float.parseFloat(fads[0].trim());
                        animation.toAlpha = (int) Float.parseFloat(fads[1].trim());
                        animation.durationStart = (int) Float.parseFloat(fads[3].trim());
                        animation.durationEnd = (int) Float.parseFloat(fads[4].trim());
                        addAnimation(animation);
                        animation = new SubtitlesAnimation.AlphaFadeAnimation();
                        animation.fromAlpha = (int) Float.parseFloat(fads[2].trim());
                        animation.toAlpha = (int) Float.parseFloat(fads[3].trim());
                        animation.durationStart = (int) Float.parseFloat(fads[5].trim());
                        animation.durationEnd = (int) Float.parseFloat(fads[6].trim());
                        addAnimation(animation);
                    }
                }
            } catch (Exception e) {
                System.err.println("error parse fade:" + fade);
            }
        }

        private void addAnimation(SubtitlesAnimation animation) {
            if (this.animation != null) {
                SubtitlesAnimation item = this.animation;
                while (item.next != null) {
                    item = item.next;
                }
                item.next = animation;
            } else {
                this.animation = animation;
            }
        }

        public boolean hasMoveAnimation() {
            SubtitlesAnimation animation = this.animation;
            while (animation != null) {
                if (animation instanceof SubtitlesAnimation.MoveAnimation) {
                    return true;
                }
                animation = animation.next;
            }
            return false;
        }

        public SubtitlesAnimation getAnimation() {
            return animation;
        }

        @Override
        public String toString() {
            return "SubtitlesStyle{" +
                    "gravity=" + gravity +
                    ", font='" + font + '\'' +
                    ", fontFall=" + fontFall +
                    ", fontSize=" + fontSize +
                    ", fontAlpha=" + (fontAlpha == null ? null : Integer.toHexString(fontAlpha)) +
                    ", fontColor=" + (fontColor == null ? null : Integer.toHexString(fontColor)) +
                    ", fontSecondaryAlpha=" + (fontSecondaryAlpha == null ? null : Integer.toHexString(fontSecondaryAlpha)) +
                    ", fontSecondaryColor=" + (fontSecondaryColor == null ? null : Integer.toHexString(fontSecondaryColor)) +
                    ", borderAlpha=" + (borderAlpha == null ? null : Integer.toHexString(borderAlpha)) +
                    ", borderColor=" + (borderColor == null ? null : Integer.toHexString(borderColor)) +
                    ", shadowAlpha=" + (shadowAlpha == null ? null : Integer.toHexString(shadowAlpha)) +
                    ", shadowColor=" + (shadowColor == null ? null : Integer.toHexString(shadowColor)) +
                    ", borderWidth=" + borderWidth +
                    ", shadowWidth=" + shadowWidth +
                    ", borderStyle=" + borderStyle +
                    ", space=" + space +
                    ", isBold=" + isBold +
                    ", isItalic=" + isItalic +
                    ", isUnderline=" + isUnderline +
                    ", isStrikeout=" + isStrikeout +
                    ", positionX=" + positionX +
                    ", positionY=" + positionY +
                    ", clip=" + clip +
                    ", blur=" + blur +
                    ", be=" + be +
                    ", marginL=" + marginL +
                    ", marginR=" + marginR +
                    ", marginV=" + marginV +
                    ", wrap=" + wrap +
                    ", durationT1=" + durationT1 +
                    ", durationT2=" + durationT2 +
                    ", durationK=" + durationK +
                    ", degree=" + degree +
                    ", degreeX=" + degreeX +
                    ", degreeY=" + degreeY +
                    ", faX=" + faX +
                    ", faY=" + faY +
                    ", scaleX=" + scaleX +
                    ", scaleY=" + scaleY +
                    ", playResX=" + playResX +
                    ", playResY=" + playResY +
                    ", isPath=" + isPath +
                    ", animation=" + animation +
                    '}';
        }
    }

    public static class Path {

        public char cmd;
        public List<Point> points;

        @Override
        public String toString() {
            return "Path{" +
                    "cmd='" + cmd + '\'' +
                    ", points=" + points +
                    '}';
        }
    }

    public static class Point {
        public float x;
        public float y;

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static class Extent {
        public float left;
        public float top;
        public float right;
        public float bottom;

        public Extent() {
        }

        public Extent(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public final float width() {
            return right - left;
        }

        public final float height() {
            return bottom - top;
        }

        @Override
        public String toString() {
            return "Extent{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    '}';
        }
    }
}
