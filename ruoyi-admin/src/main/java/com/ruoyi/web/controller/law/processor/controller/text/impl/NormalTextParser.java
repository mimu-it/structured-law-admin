package com.ruoyi.web.controller.law.processor.controller.text.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;
import com.ruoyi.web.controller.law.processor.controller.text.IParser;

import java.util.*;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public class NormalTextParser implements IParser {

    @Override
    public void parse(List<String> words, IntegralParams integralParams) {
        List<String> keywords = new ArrayList<>();
        Set<String> wordMatchSet = new HashSet<>();
        words.forEach((word) -> {
            keywords.add(word);
            wordMatchSet.add(word);
        });

        this.cleanWords(words, wordMatchSet);
        integralParams.setTermText(CollectionUtil.join(keywords, " "));
    }
}
