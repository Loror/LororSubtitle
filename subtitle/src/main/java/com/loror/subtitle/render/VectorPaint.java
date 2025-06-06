package com.loror.subtitle.render;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.loror.subtitle.SubtitlesDecoder;
import com.loror.subtitle.model.SubtitlesModel;

import java.util.List;

public class VectorPaint {

    private final Paint vectorPaint = new Paint();

    public VectorPaint() {
        vectorPaint.setAntiAlias(true);
        vectorPaint.setColor(Color.RED);
        vectorPaint.setStyle(Paint.Style.FILL);
    }

    private int videoWidth = 0;
    private int videoHeight = 0;
    private Rect area;

    /**
     * 设置视频宽高
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public void setArea(Rect area) {
        this.area = area;
    }

    /**
     * 绘制path
     */
    public void drawVector(SubtitlesModel model, @NonNull Canvas canvas) {
        Style style = model.style;
        if (style == null || area == null) {
            return;
        }
        List<SubtitlesModel.Path> paths = model.getPaths();
        boolean isBe = style.getBe() > 0;
        if (paths != null) {
            measureRange(paths, style);
            if (style.hasBorderWidth()) {
                vectorPaint.setStyle(Paint.Style.STROKE);
                vectorPaint.setStrokeWidth(style.getBorderWidth());
                int borderColor = Color.BLACK;
                if (style.hasBorderColor()) {
                    borderColor = style.getBorderColor();
                }
                vectorPaint.setColor(borderColor);
                float be = 0;
                if (isBe) {
                    be = style.getBe();
                    if (be == 1) {
                        be = vectorPaint.getTextSize() / 20f;
                    } else {
//                        be = isScaleBs ? pt2Px(be) : be;
                    }
                }
                if (be == 0 && style != null && style.currentBlur > 0) {
                    be = style.currentBlur;
                }
                if (be > 0) {
                    vectorPaint.setMaskFilter(new BlurMaskFilter(be, BlurMaskFilter.Blur.OUTER));
                }
                draw(canvas, paths, style);
            }
            vectorPaint.setStyle(Paint.Style.FILL);
            vectorPaint.setMaskFilter(null);
            if (style.hasFontColor()) {
                vectorPaint.setColor(style.getFontColor());
            }
            draw(canvas, paths, style);
        }
    }

    private void draw(@NonNull Canvas canvas, List<SubtitlesModel.Path> paths, Style extra) {
        Path path = new Path();
        boolean first = true;
        for (SubtitlesModel.Path item : paths) {
            switch (item.cmd) {
                // m <x> <y> 将鼠标移至坐标(x,y)，同时将现有的图形封闭(即开始画新的图形)，所有绘画都以这个命令开始
                case 'm':
                    canvas.drawPath(path, vectorPaint);
                    if (first) {
                        path.reset();
                    } else {
                        path.close();
                    }
                    path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                    first = false;
                    break;
                //n <x> <y> 将鼠标移至坐标(x,y)，同时不封闭原有的图形
                case 'n':
                    path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                    break;
                //l <x> <y> 从鼠标原来的坐标位置画一条直线到(x,y)，并从这个点继续绘画
                case 'l':
                    path.lineTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                    break;
                //b <x1> <y1> <x2> <y2> <x3> <y3> 画一条三度贝塞尔曲线至(x3,y3)，以(x1,y1)，(x2,y2)作为控制点
                case 'b':
                    if (item.points.size() == 3) {
//                        path.reset();
                        path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                        path.quadTo(getPointX(item.points.get(1).x, extra), getPointY(item.points.get(1).y, extra),
                                getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                    }
                    break;
                //s <x1> <y1> <x2> <y2> <x3> <y3> ... <xN> <yN> 从现有坐标画一条“三次均匀B样条”(cubic uniform b-spline)到点(xN,yN)
                // 该命令至少要含有三个坐标点(三个坐标时等同于贝塞尔曲线)
                // 这个命令实质上是把几条贝塞尔曲线连结到一起
                case 's':
                    if (item.points.size() == 3) {
//                        path.reset();
                        path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                        path.quadTo(getPointX(item.points.get(1).x, extra), getPointY(item.points.get(1).y, extra),
                                getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                    } else {
                        for (SubtitlesModel.Point point : item.points) {
                            path.lineTo(getPointX(point.x, extra), getPointY(point.y, extra));
                        }
                    }
                    break;
                //p <x> <y> 延长B样条(b-spline)到点(x,y)，作用相当于在s命令后多加一个坐标点(x,y)
                case 'p':
                    for (SubtitlesModel.Point point : item.points) {
                        path.lineTo(getPointX(point.x, extra), getPointY(point.y, extra));
                    }
                    break;
                //c 结束B样条(b-spline)
                case 'c':
                    canvas.drawPath(path, vectorPaint);
                    path.reset();
                    break;
            }
        }
        path.close();
        canvas.drawPath(path, vectorPaint);
    }

    private final Rect range = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * 计算绘制宽高
     */
    private void measureRange(List<SubtitlesModel.Path> paths, Style extra) {
        range.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        float scale = (float) Math.pow(2, extra.pathScale - 1);
        for (int i = 0; i < paths.size(); i++) {
            SubtitlesModel.Path path = paths.get(i);
            if (path.points != null) {
                for (SubtitlesModel.Point point : path.points) {
                    float x = area.width() * 1f / extra.playResX * point.x / scale;
                    float y = area.height() * 1f / extra.playResY * point.y / scale;
                    if (range.left == Integer.MAX_VALUE) {
                        range.left = (int) x;
                    } else if (x < range.left) {
                        range.left = (int) x;
                    }
                    if (range.right == Integer.MAX_VALUE) {
                        range.right = (int) x;
                    } else if (x > range.right) {
                        range.right = (int) x;
                    }
                    if (range.top == Integer.MAX_VALUE) {
                        range.top = (int) y;
                    } else if (y < range.top) {
                        range.top = (int) y;
                    }
                    if (range.bottom == Integer.MAX_VALUE) {
                        range.bottom = (int) y;
                    } else if (y > range.bottom) {
                        range.bottom = (int) y;
                    }
                }
            }
        }
    }

    private float getPointX(float dp, Style extra) {
        float scale = (float) Math.pow(2, extra.pathScale - 1);
        float x = area.width() * 1f / extra.playResX * dp / scale + (area.width() * extra.x) + area.left;
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
            //右边
            x -= range.width() / 2f;
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN3 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            //中间
            x -= range.width();
        } else {
            //左边
        }
        return x;
    }

    private float getPointY(float dp, Style extra) {
        float scale = (float) Math.pow(2, extra.pathScale - 1);
        float y = area.height() * 1f / extra.playResY * dp / scale + (area.height() * extra.y) + area.top;
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN7 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            //顶部
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN1 || extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN3) {
            //底部
            y -= range.height();
        } else {
            //中部
            y -= range.height() / 2f;
        }
        return y;
    }
}
