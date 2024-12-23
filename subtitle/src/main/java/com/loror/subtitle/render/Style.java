package com.loror.subtitle.render;

import com.loror.subtitle.model.SubtitlesModel;

/***********************render绘制后样式********************/
public class Style extends SubtitlesModel.SubtitlesStyle {
    public boolean scaleBS;//是否缩放字幕边框、阴影，为false则使用绝对像素
    public float x;//相对屏幕坐标比例0~1f
    public float y;//相对屏幕坐标比例0~1f
    public boolean useSecond;//是否使用卡拉OK字体颜色
    public float currentScaleX = 1;//当前x轴缩放比例0~1f
    public float currentScaleY = 1;//当前y轴缩放比例0~1f
    public float alpha = 1;//透明度 0~1，由render绘制
    public SubtitlesModel.Extent currentClip;//裁剪
    public float currentFontScale = 1;//字体大小整体缩放
    public float currentDegree;//角度
    public float currentDegreeX;//角度
    public float currentDegreeY;//角度
    public float currentBlur;//主体模糊

    public Style() {
        super();
    }

    public Style(SubtitlesModel.SubtitlesStyle style) {
        super(style);
    }

    @Override
    public String toString() {
        return "Style{" +
                "x=" + x +
                ", y=" + y +
                ", useSecond=" + useSecond +
                ", currentScaleX=" + currentScaleX +
                ", currentScaleY=" + currentScaleY +
                ", alpha=" + alpha +
                ", currentClip=" + currentClip +
                ", currentFontScale=" + currentFontScale +
                ", currentDegree=" + currentDegree +
                ", currentDegreeX=" + currentDegreeX +
                ", currentDegreeY=" + currentDegreeY +
                ", currentBlur=" + currentBlur +
                '}'
                + super.toString();
    }
}
