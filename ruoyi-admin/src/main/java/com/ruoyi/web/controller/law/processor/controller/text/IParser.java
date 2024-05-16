package com.ruoyi.web.controller.law.processor.controller.text;

import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public interface IParser {

    /**
     * 文法解释
     * @param words
     * @param integralParams
     */
    void parse(List<String> words, IntegralParams integralParams);


    /**
     *
     * @param words
     * @param matchList
     */
    default void cleanWords(List<String> words, Set<String> matchList) {
        Iterator<String> iterator = words.iterator();
        while(iterator.hasNext()) {
            String word = iterator.next();
            if(matchList.contains(word)) {
                iterator.remove();
            }
        }
    }
}
