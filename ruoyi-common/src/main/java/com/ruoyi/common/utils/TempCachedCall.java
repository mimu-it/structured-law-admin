package com.ruoyi.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author xiao.hu
 * @date 2023-12-24
 * @apiNote
 */
public class TempCachedCall<T, R> {
    Map<T, R> tempCache = new HashMap<>();

    public R call(T parameter, Function<? super T, ? extends R> mapper) {
        R value =  tempCache.get(parameter);
        if(value != null) {
            return value;
        }

        value = mapper.apply(parameter);
        tempCache.put(parameter, value);
        return value;
    }
}
