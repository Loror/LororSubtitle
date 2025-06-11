package com.loror.subtitle.util;

import com.loror.subtitle.model.SubtitlesModel;

import java.util.ArrayList;
import java.util.List;

public class SubtitlesUtil {

    public static final int SEARCH_MAX = 50;

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
        if (index >= 0) {
            //model根据start排序，不能保证与time完全顺序，无法判定搜索结束，限制最大搜索范围
            for (int i = Math.max(0, index - SEARCH_MAX); i < Math.min(index + SEARCH_MAX, list.size()); i++) {
                SubtitlesModel item = list.get(i);
                if (time >= item.star && time <= item.end) {
                    result.add(item);
                }
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
