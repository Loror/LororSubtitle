package com.loror.subtitle.model;

import androidx.annotation.NonNull;

public class LrcBean implements Comparable<LrcBean> {

    private int beginTime;// 开始时间
    private String lrc;//歌曲
    private int lineNum;//行数,如果相同时间多句歌词，就用行数来排序

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    @Override
    public int compareTo(@NonNull LrcBean another) {
        if (this.beginTime == 0 && another.beginTime == 0) {
            return Integer.compare(this.getLineNum(), another.getLineNum());
        } else {
            return Integer.compare(this.beginTime, another.beginTime);
        }
    }

    @Override
    public String toString() {
        return "LrcBean{" +
                "beginTime=" + beginTime +
                ", lrc='" + lrc + '\'' +
                ", lineNum=" + lineNum +
                '}';
    }
}
