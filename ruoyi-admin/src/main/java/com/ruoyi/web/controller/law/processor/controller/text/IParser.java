package com.baymax.bone.service.legislation.parser.text;

import com.baymax.bone.service.legislation.core.pojo.LawSearchParams;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public interface IParser {

    void parse(List<String> words, LawSearchParams lawSearchParams);


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
