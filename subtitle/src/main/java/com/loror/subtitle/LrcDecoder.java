package com.loror.subtitle;

import android.text.TextUtils;
import android.util.Log;

import com.loror.subtitle.model.LrcBean;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 歌词解析
 */
public class LrcDecoder {

    public static final String TAG = "LrcDecoder";

    private final List<LrcBean> mLrcs = new ArrayList<>();

    /**
     * 获取全屏播放器歌词
     */
    public List<LrcBean> getLrcs() {
        return mLrcs;
    }

    public void clear() {
        mLrcs.clear();
    }

    public List<LrcBean> decode(File file) {
        mLrcs.clear();
        try {
            if (file.exists()) {
                parse(new FileInputStream(file), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLrcs.clear();
        }
        Collections.sort(mLrcs);
        return mLrcs;
    }

    public List<LrcBean> decode(InputStream inputStream, String charset) {
        mLrcs.clear();
        try {
            parse(inputStream, charset);
        } catch (Exception e) {
            e.printStackTrace();
            mLrcs.clear();
        }
        Collections.sort(mLrcs);
        return mLrcs;
    }

    private void parse(InputStream mFileInputStream, String charset) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mFileInputStream, charset));
        try {
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) {
                    parseLine(line, i);
                }
                i++;
            }
        } finally {
            close(mFileInputStream);
            close(bufferedReader);
        }
    }

    /**
     * 每一句歌词解析
     * <p>
     * ID标签
     * [al:专辑名]
     * [ar:歌手名]
     * [au:歌词作者-作曲家]
     * [by:此LRC文件的创建者]
     * [offset:+/- 时间补偿值，以毫秒为单位，正值表示加快，负值表示延后]
     * [re:创建此LRC文件的播放器或编辑器]
     * [ti:歌词(歌曲)的标题]
     * [ve:程序的版本]
     * <p>
     * 歌词标签
     * [00:17.20][00:25.44]自从你离开我
     * [00:25.44]自从你离开我
     */
    private void parseLine(String s, int index) {
        if (s.contains("]")) {//[ti:不流泪(AqmaYishim)]
            String[] split = s.split("]");//时间和歌词，可能有多个时间 [02:06.22 [04:00.12  也不需要魅力的长发
            List<String> tempTimes = new ArrayList<>();//临时保存时间
            String templrc = "";
            for (String timeLrc : split) {
                if (timeLrc.contains("[")) {
                    tempTimes.add(timeLrc.replace("[", "").trim());
                } else {
                    templrc = timeLrc.trim();
                }
            }
            if (!tempTimes.isEmpty() && !TextUtils.isEmpty(templrc)) {// 必须保证时间和歌词同时存在才有意义显示歌词
                for (int i = 0, size = tempTimes.size(); i < size; i++) {
                    try {
                        String time = tempTimes.get(i);
                        LrcBean mLrcBean = new LrcBean();
                        mLrcBean.setLrc(templrc);
                        mLrcBean.setLineNum(index);
                        mLrcBean.setBeginTime(formatTime(time));
                        mLrcs.add(mLrcBean);
                    } catch (Exception e) {
                        Log.e(TAG, "解析行出错：" + s);
                    }
                }
            }
        } else {
            Log.e(TAG, "歌词格式错误:" + s);
        }
    }

    /**
     * 时间标签的格式是[mm:ss.xx]，其中mm是分钟数，ss是秒数，xx是10ms数
     */
    private int formatTime(String time) {
        time = time.replace('.', ':');
        String[] times = time.split(":");
        return (Integer.parseInt(times[0]) * 60 * 1000)
                + (Integer.parseInt(times[1]) * 1000)
                + (Integer.parseInt(times[2]) * (times[2].length() == 2 ? 10 : 1));
    }

    private void close(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
