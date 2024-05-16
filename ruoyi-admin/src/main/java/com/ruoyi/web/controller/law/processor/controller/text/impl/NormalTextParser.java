package com.baymax.bone.service.legislation.parser.text.impl;

import com.baymax.bone.service.legislation.core.pojo.LawSearchParams;
import com.baymax.bone.service.legislation.parser.text.IParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public class NormalTextParser implements IParser {

    @Override
    public void parse(List<String> words, LawSearchParams lawSearchParams) {
        List<String> keywords = new ArrayList<>();
        Set<String> wordMatchSet = new HashSet<>();
        words.forEach((word) -> {
            keywords.add(word);
            wordMatchSet.add(word);
        });

        this.cleanWords(words, wordMatchSet);
        lawSearchParams.setProvisionText(keywords);
    }
}
