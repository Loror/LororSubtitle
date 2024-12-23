package com.loror.subtitle.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

import com.loror.subtitle.model.SubtitlesModel;

import java.util.List;

public class VectorPaint {

    private final Paint vectorPaint = new Paint();
    private int width;
    private int height;

    public VectorPaint() {
        vectorPaint.setAntiAlias(true);
        vectorPaint.setColor(Color.RED);
        vectorPaint.setStyle(Paint.Style.FILL);
    }

    public void setScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 绘图命令：
     * m <x> <y> 将鼠标移至坐标(x,y)，同时将现有的图形封闭(即开始画新的图形)，所有绘画都以这个命令开始
     * n <x> <y> 将鼠标移至坐标(x,y)，同时不封闭原有的图形
     * l <x> <y> 从鼠标原来的坐标位置画一条直线到(x,y)，并从这个点继续绘画
     * b <x1> <y1> <x2> <y2> <x3> <y3> 画一条三度贝塞尔曲线至(x3,y3)，以(x1,y1)，(x2,y2)作为控制点
     * s <x1> <y1> <x2> <y2> <x3> <y3> ... <xN> <yN> 从现有坐标画一条“三次均匀B样条”(cubic uniform b-spline)到点(xN,yN)
     * 该命令至少要含有三个坐标点(三个坐标时等同于贝塞尔曲线)
     * 这个命令实质上是把几条贝塞尔曲线连结到一起
     * p <x> <y> 延长B样条(b-spline)到点(x,y)，作用相当于在s命令后多加一个坐标点(x,y)
     * c 结束B样条(b-spline)
     */
    public void drawVector(SubtitlesModel model, @NonNull Canvas canvas) {
        if (model.extra == null) {
            return;
        }
        Style extra = model.style;
        List<SubtitlesModel.Path> paths = model.getPaths();
        if (paths != null) {
            if (extra.hasFontColor()) {
                vectorPaint.setColor(extra.getFontColor());
            }
            Path path = new Path();
            for (SubtitlesModel.Path item : paths) {
                if ("m".equals(item.cmd)) {
                    canvas.drawPath(path, vectorPaint);
                    path.reset();
                    path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                } else if ("n".equals(item.cmd)) {
                    path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                } else if ("l".equals(item.cmd)) {
                    path.lineTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                } else if ("b".equals(item.cmd)) {
                    if (item.points.size() == 3) {
//                        path.reset();
                        path.moveTo(getPointX(item.points.get(0).x, extra), getPointY(item.points.get(0).y, extra));
                        path.quadTo(getPointX(item.points.get(1).x, extra), getPointY(item.points.get(1).y, extra),
                                getPointX(item.points.get(2).x, extra), getPointY(item.points.get(2).y, extra));
                    }
                } else if ("s".equals(item.cmd)) {
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
                } else if ("p".equals(item.cmd)) {
                    for (SubtitlesModel.Point point : item.points) {
                        path.lineTo(getPointX(point.x, extra), getPointY(point.y, extra));
                    }
                } else if ("c".equals(item.cmd)) {
                    canvas.drawPath(path, vectorPaint);
                    path.reset();
                }
            }
            path.close();
            canvas.drawPath(path, vectorPaint);
        }
    }

    private float getPointX(float dp, Style extra) {
        float scale = (float) Math.pow(2, extra.pathScale - 1) * 2;
        return extra.playResX * 1f / getWidth() * dp / scale + extra.x * getWidth();
    }

    private float getPointY(float dp, Style extra) {
        float scale = (float) Math.pow(2, extra.pathScale - 1) * 2;
        return extra.playResX * 1f / getWidth() * dp / scale + extra.y * getHeight();
    }
}
