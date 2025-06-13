package com.loror.subtitle.render;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.loror.subtitle.SubtitlesDecoder;
import com.loror.subtitle.model.SubtitlesModel;
import com.loror.subtitle.util.SubColorUtil;

import java.util.List;

public class VectorPaint {

    private final Paint vectorPaint = new Paint();

    public VectorPaint() {
        vectorPaint.setAntiAlias(true);
        vectorPaint.setColor(Color.RED);
        vectorPaint.setStyle(Paint.Style.FILL);
    }

    private int videoWidth;
    private int videoHeight;
    private int screenWidth = 1920;
    private int screenHeight = 1080;
    private int ptWidth;
    private int ptHeight;
    private Rect area;

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
     * 设置view宽高
     */
    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setArea(Rect area) {
        this.area = area;
    }

    private float authorX;
    private float authorY;

    /**
     * 绘制path
     */
    public void drawVector(SubtitlesModel model, @NonNull Canvas canvas) {
        Style style = model.style;
        if (style == null || area == null) {
            return;
        }
        authorX = (area.width() * style.x) + area.left;
        authorY = (area.height() * style.y) + area.top;
        List<SubtitlesModel.Path> paths = model.getPaths();
        boolean isScaleBs = style.scaleBS;
        boolean isBe = style.getBe() > 0;
        if (paths != null) {
            measureRange(paths, style);
            if (style.hasBorderWidth() && style.getBorderWidth() > 0) {
                vectorPaint.setStyle(Paint.Style.STROKE);
                vectorPaint.setStrokeWidth(style.getBorderWidth());
                int borderColor = Color.BLACK;
                if (style.hasBorderColor()) {
                    if (style.alpha != 1) {
                        borderColor = style.getBorderColor();
                    } else {
                        borderColor = SubColorUtil.replaceColorAlpha(borderColor, (int) (255 * style.alpha));
                    }
                    vectorPaint.setColor(borderColor);
                    float be = 0;
                    if (isBe) {
                        be = style.getBe();
                        if (be == 1) {
                            be = vectorPaint.getTextSize() / 20f;
                        } else {
                            be = isScaleBs ? pt2Px(be) : be;
                        }
                    }
                    if (be == 0 && style.currentBlur > 0) {
                        be = style.currentBlur;
                    }
                    if (be > 0) {
                        vectorPaint.setMaskFilter(new BlurMaskFilter(be, BlurMaskFilter.Blur.OUTER));
                    }
                    draw(canvas, paths, style);
                }
            }
            vectorPaint.setStyle(Paint.Style.FILL);
            vectorPaint.setMaskFilter(null);
            //注意\be标签只会模糊文本的边框 ，不是整体。
            //注意\blur如果没有边框，那么文本整体就会被模糊。
            if (style.currentBlur > 0 && vectorPaint.getStrokeWidth() == 0) {
                vectorPaint.setMaskFilter(new BlurMaskFilter(pt2Px(style.currentBlur), BlurMaskFilter.Blur.NORMAL));
            }
            if (style.hasFontColor()) {
                int fontColor = style.getFontColor();
                if (style.alpha != 1) {
                    fontColor = SubColorUtil.replaceColorAlpha(fontColor, (int) (255 * style.alpha));
                }
                vectorPaint.setColor(fontColor);
            } else {
                vectorPaint.setMaskFilter(null);
                return;
            }
            draw(canvas, paths, style);
            vectorPaint.setMaskFilter(null);
        }
    }

    private void draw(@NonNull Canvas canvas, List<SubtitlesModel.Path> paths, Style extra) {
        Path path = new Path();
        boolean flag = false;
        for (SubtitlesModel.Path item : paths) {
            switch (item.cmd) {
                // m <x> <y> 将鼠标移至坐标(x,y)，同时将现有的图形封闭(即开始画新的图形)，所有绘画都以这个命令开始
                case 'm':
                    if (flag) {
                        path.close();
                        canvas.drawPath(path, vectorPaint);
                        path.reset();
                    }
                    path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                    flag = true;
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
                        path.cubicTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra),
                                getPointX(item.points.get(1).x, extra), getPointY(item.points.get(1).y, extra),
                                getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                        path.lineTo(getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                    }
                    break;
                //s <x1> <y1> <x2> <y2> <x3> <y3> ... <xN> <yN> 从现有坐标画一条“三次均匀B样条”(cubic uniform b-spline)到点(xN,yN)
                // 该命令至少要含有三个坐标点(三个坐标时等同于贝塞尔曲线)
                // 这个命令实质上是把几条贝塞尔曲线连结到一起
                case 's':
                    if (item.points.size() == 3) {
//                        path.reset();
                        path.cubicTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra),
                                getPointX(item.points.get(1).x, extra), getPointY(item.points.get(1).y, extra),
                                getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                        path.lineTo(getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                    } else if (item.points.size() > 3) {
                        PointF startPoint = calculateBSplinePoint(item.points, extra, 0, 0); // 起始点
                        path.lineTo(startPoint.x, startPoint.y);

                        // 遍历所有曲线段
                        for (int i = 0; i <= item.points.size() - 4; i++) {
                            // 每段曲线使用50个采样点（平滑度）
                            for (int j = 1; j <= 50; j++) {
                                float t = j / 50.0f; // 参数t ∈ [0, 1]
                                PointF point = calculateBSplinePoint(item.points, extra, i, t);
                                path.lineTo(point.x, point.y);
                            }
                        }
//                        canvas.drawPath(path, vectorPaint);
                        path.lineTo(getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
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
                    path.close();
                    canvas.drawPath(path, vectorPaint);
                    path.reset();
                    break;
            }
        }
        path.close();
        canvas.drawPath(path, vectorPaint);
    }

    // 计算曲线上点（i为段索引，t为参数）
    private PointF calculateBSplinePoint(List<SubtitlesModel.Point> points, Style extra, int i, float t) {
        PointF p0 = new PointF(getPointX(points.get(i).x, extra), getPointY(points.get(i).y, extra));
        PointF p1 = new PointF(getPointX(points.get(i + 1).x, extra), getPointY(points.get(i + 1).y, extra));
        PointF p2 = new PointF(getPointX(points.get(i + 2).x, extra), getPointY(points.get(i + 2).y, extra));
        PointF p3 = new PointF(getPointX(points.get(i + 3).x, extra), getPointY(points.get(i + 3).y, extra));

        float t2 = t * t;
        float t3 = t2 * t;

        // 三次均匀B样条基函数
        float b0 = (1 - t) * (1 - t) * (1 - t) / 6.0f;
        float b1 = (3 * t3 - 6 * t2 + 4) / 6.0f;
        float b2 = (-3 * t3 + 3 * t2 + 3 * t + 1) / 6.0f;
        float b3 = t3 / 6.0f;

        // 计算点坐标（基函数加权和）
        float x = b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x;
        float y = b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y;

        return new PointF(x, y);
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
                    float x = area.width() * 1f / extra.playResX * point.x / scale + authorX;
                    float y = area.height() * 1f / extra.playResY * point.y / scale + authorY;
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
        float author = authorX;
        float x = area.width() * 1f / extra.playResX * dp / scale + author;
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN5 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_UNSET) {
            //中间
            x -= range.width() / 2f;
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN3 || extra.gravity == SubtitlesDecoder.GRAVITY_AN6 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            //右边
            x -= range.width();
        } else {
            //左边
        }
        if (extra.currentScaleX != 1) {
            x = author + (x - author) * extra.currentScaleX;
        }
        return x;
    }

    private float getPointY(float dp, Style extra) {
        float scale = (float) Math.pow(2, extra.pathScale - 1);
        float author = authorY;
        float y = area.height() * 1f / extra.playResY * dp / scale + author;
        if (extra.gravity == SubtitlesDecoder.GRAVITY_AN7 || extra.gravity == SubtitlesDecoder.GRAVITY_AN8 || extra.gravity == SubtitlesDecoder.GRAVITY_AN9) {
            //顶部
        } else if (extra.gravity == SubtitlesDecoder.GRAVITY_AN1 || extra.gravity == SubtitlesDecoder.GRAVITY_AN2 || extra.gravity == SubtitlesDecoder.GRAVITY_AN3) {
            //底部
            y -= range.height();
        } else {
            //中部
            y -= range.height() / 2f;
        }
        if (extra.currentScaleY != 1) {
            y = author + (y - author) * extra.currentScaleY;
        }
        return y;
    }

    /**
     * ass字幕字体大小为pt
     * 字幕定义为字高
     */
    public float pt2Px(float pt) {
        int height = screenHeight;
        if (height == 0) {
            return pt;
        }
        if (ptHeight != 0) {
            float scale = height * 1f / ptHeight;
            return pt * scale;
        }
        float scale = height * 1f / 288;//字幕默认定义384*288
        return pt * scale;
    }
}
