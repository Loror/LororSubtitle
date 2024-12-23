package com.loror.subtitle.util;

import com.loror.subtitle.model.SubtitlesModel;

import java.util.ArrayList;
import java.util.List;

public class SubtitlesUtil {

    /**
     * 查找满足当前时间的所有字幕
     */
    public static List<SubtitlesModel> searchSubs(List<SubtitlesModel> list, int time) {
        SubtitlesModel target = searchSub(list, time);
        if (target == null) {
            return null;
        }
        int index = target.sort;
        if (index < 0) {
            index = list.indexOf(target);
        }
        List<SubtitlesModel> result = new ArrayList<>();
        for (int i = Math.max(0, index - 10); i < Math.min(index + 10, list.size()); i++) {
            SubtitlesModel item = list.get(i);
            if (time >= item.star && time <= item.end) {
                result.add(item);
            }
        }
        if (result.isEmpty()) {
            result.add(target);
        }
        return result;
    }

    /**
     * 采用二分法去查找当前应该播放的字幕
     *
     * @param list 全部字幕
     * @param key  播放的时间点
     * @return
     */
    public static SubtitlesModel searchSub(List<SubtitlesModel> list, int key) {
        int start = 0;
        int end = list.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            if (key < list.get(middle).star) {
                if (key > list.get(middle).end) {
                    return list.get(middle);
                }
                end = middle - 1;
            } else if (key > list.get(middle).end) {
                if (key < list.get(middle).star) {
                    return list.get(middle);
                }
                start = middle + 1;
            } else if (key >= list.get(middle).star && key <= list.get(middle).end) {
                return list.get(middle);
            }
        }
        return null;
    }
}
