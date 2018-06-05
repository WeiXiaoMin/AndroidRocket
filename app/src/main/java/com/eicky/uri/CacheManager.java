package com.eicky.uri;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eicky.util.ACache;

import java.io.File;
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
        this(context, key, limitCount, null);
    }

    CacheManager(Context context, String key, int limitCount, @Nullable File cacheDir) {
        if (cacheDir == null) {
            aCache = ACache.get(context);
        } else {
            aCache = ACache.get(cacheDir);
        }

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
                        while (limitCount < this.list.size()) {
                            this.list.removeLast();
                        }
                    } catch (ClassCastException e) {
                        aCache.remove(key);
                    }
                }
            }
        }
    }

    void add(@NonNull T t) {
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

    void remove(int index) {
        if (index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }

    void clear() {
        list.clear();
    }

    void addAll(@NonNull List<T> list) {
        for (T t : list) {
            add(t);
        }
    }

    T get(int index) {
        return list.get(index);
    }

    LinkedList<T> getList() {
        return list;
    }

    void executeCache() {
        aCache.put(key, list);
    }
}
