package com.eicky.uri;

import android.content.Context;
import android.text.TextUtils;

import com.eicky.ACache;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: Wei xiao min
 * Email: weixiaomin02@maoyan.com
 * Date: 2018/5/14
 */
public class CacheManager<T extends Serializable> {
    private final String key;
    private final int limitCount;
    private final ACache aCache;
    private LinkedList<T> list;

    CacheManager(Context context, String key, int limitCount) {
        aCache = ACache.get(context);
        this.key = key;
        this.limitCount = limitCount;
        list = new LinkedList<>();
        if (!TextUtils.isEmpty(key)) {
            Object object = aCache.getAsObject(key);
            if (object != null) {
                if (object instanceof List) {
                    try {
                        List<T> objs = (List<T>) object;
                        this.list.addAll(objs);
                    } catch (ClassCastException e) {
                        aCache.remove(key);
                    }
                }
            }
        }
    }

    void add(T t) {
        if (list.contains(t)) {
            if (list.indexOf(t) > 0) {
                list.remove(t);
                list.add(0, t);
            }
            return;
        }
        while (list.size() >= limitCount) {
            int index = list.size() - 1;
            list.remove(index);
        }
        list.add(0, t);
    }

    T get(int index) {
        return list.get(index);
    }

    LinkedList<T> getList() {
        return list;
    }

    void executeCache() {
        JSONArray jsonArray = new JSONArray(list);
        aCache.put(key, jsonArray);
    }
}
