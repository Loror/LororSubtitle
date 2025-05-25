package com.loror.subtitle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RenderedModel {

    public int gravity;

    private List<List<SubtitlesModel>> models = null;
    private final Map<Integer, List<SubtitlesModel>> layerModels = new HashMap<>();

    public RenderedModel(int gravity) {
        this.gravity = gravity;
    }

    public void add(SubtitlesModel sub) {
        int layer = sub.layer;
        List<SubtitlesModel> list = layerModels.get(layer);
        if (list == null) {
            list = new ArrayList<>();
            layerModels.put(layer, list);
        }
        list.add(sub);
    }

    public void sort() {
        generateModels();
        for (List<SubtitlesModel> list : models) {
            //底部字幕，ass重排序 后来居上，时间先开始局上
            if (list != null && list.size() > 1) {
                int type = list.get(0).type;
                if (type == 1) {
                    Collections.reverse(list);
                    Collections.sort(list, (o1, o2) -> {
//                        int comp = Integer.compare(o1.layer, o2.layer);
//                        if (comp == 0) {
//                            return Long.compare(o1.star, o2.star);
//                        }
//                        return comp;
                        return Long.compare(o1.star, o2.star);
                    });
                }
            }
        }
    }

    private void generateModels() {
        if (models == null) {
            models = new ArrayList<>();
            Set<Integer> layers = new TreeSet<>(layerModels.keySet());
            for (Integer layer : layers) {
                models.add(layerModels.get(layer));
            }
        }
    }

    public int getGravity() {
        return gravity;
    }

    public List<List<SubtitlesModel>> getModels() {
        generateModels();
        return models;
    }

    @Override
    public String toString() {
        return "RenderedModel{" +
                "gravity=" + gravity +
                ", models=" + models +
                '}';
    }
}