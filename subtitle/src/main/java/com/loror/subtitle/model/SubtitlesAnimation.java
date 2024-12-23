package com.loror.subtitle.model;

import android.graphics.Rect;

public class SubtitlesAnimation {

    public int durationStart;
    public int durationEnd;
    public SubtitlesAnimation next;

    protected String nextAnimationType() {
        String nextClass = null;
        if (next != null) {
            nextClass = "";
            SubtitlesAnimation item = this;
            while (item.next != null) {
                nextClass += item.next.getClass().getSimpleName() + ",";
                item = item.next;
            }
        }
        return nextClass;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "durationStart=" + durationStart +
                ", durationEnd=" + durationEnd +
                ", next=" + nextAnimationType() +
                '}';
    }

    public static class MoveAnimation extends SubtitlesAnimation {

        public float fromX;
        public float fromY;
        public float toX;
        public float toY;

        @Override
        public String toString() {
            return "MoveAnimation{" +
                    "fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }

    public static class FadeAnimation extends SubtitlesAnimation {

    }

    public static class AlphaFadeAnimation extends SubtitlesAnimation {

        //透明度值是十进制的，介于0和255之间，当其为零时字符时完全不透明的，为255时是完全透明的。
        public int fromAlpha;
        public int toAlpha;

        @Override
        public String toString() {
            return "AlphaFadeAnimation{" +
                    "fromAlpha=" + fromAlpha +
                    ", toAlpha=" + toAlpha +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }

    public static class ClipAnimation extends SubtitlesAnimation {
        public Rect toClip;//裁剪

        @Override
        public String toString() {
            return "ClipAnimation{" +
                    "toClip=" + toClip +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }

    public static class FsAnimation extends SubtitlesAnimation {
        public int fontSize;

        @Override
        public String toString() {
            return "FsAnimation{" +
                    "fontSize=" + fontSize +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }

    public static class DegreeAnimation extends SubtitlesAnimation {

        public String type;//x,y,z
        public float degree;

        @Override
        public String toString() {
            return "DegreeAnimation{" +
                    "degree=" + degree +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }

    public static class BlurAnimation extends SubtitlesAnimation {

        public float blur;

        @Override
        public String toString() {
            return "BlurAnimation{" +
                    "blur=" + blur +
                    ", durationStart=" + durationStart +
                    ", durationEnd=" + durationEnd +
                    ", next=" + nextAnimationType() +
                    '}';
        }
    }
}
