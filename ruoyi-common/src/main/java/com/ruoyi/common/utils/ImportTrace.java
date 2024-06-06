package com.ruoyi.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-08-18
 * @apiNote
 */
public class ImportTrace {

    private final static ThreadLocal<List<String>> log = new ThreadLocal<>();
    private final static ThreadLocal<Map<String, Integer>> counter = new ThreadLocal<>();
    private final static ThreadLocal<Map<String, List<String>>> collectMap = new ThreadLocal<>();

    /**
     *
     */
    public static void prepare() {
        clear();
    }

    /**
     *
     */
    public static void clear() {
        log.remove();
        counter.remove();
        collectMap.remove();
    }

    /**
     *
     * @param template
     * @param args
     */
    public static void record(String template, Object... args) {
        if(log.get() == null) {
            log.set(new ArrayList<>());
        }

        log.get().add(String.format(template, args));
    }

    /**
     *
     * @return
     */
    public static List<String> getTrace() {
        return log.get();
    }

    /**
     *
     * @param key
     */
    public static void increment(String key) {
        Map<String, Integer> map = counter.get();
        if(map == null) {
            map = new HashMap<>(2);
            counter.set(map);
        }

        Integer number = map.get(key);
        if(number == null) {
            number = 0;
            map.put(key, number);
        }

        map.put(key, ++number);
    }

    /**
     *
     * @param key
     * @return
     */
    public static Integer getCounter(String key) {
        Integer count = counter.get().get(key);
        if(count == null) {
            return 0;
        }

        return count;
    }


    /**
     *
     * @param key
     * @param template
     * @param args
     */
    public static void record(String key, String template, Object... args) {
        Map<String, List<String>> map = collectMap.get();
        if(map == null) {
            map = new HashMap<>();
            collectMap.set(map);
        }

        List<String> list = map.get(key);
        if(list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }

        list.add(String.format(template, args));
    }

    /**
     *
     * @return
     */
    public static List<String> getTrace(String key) {
        Map<String, List<String>> map = collectMap.get();
        if(map == null) {
            return null;
        }

        return map.get(key);
    }
}
