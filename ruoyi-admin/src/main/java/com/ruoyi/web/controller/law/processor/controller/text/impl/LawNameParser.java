package com.baymax.bone.service.legislation.parser.text.impl;

import com.baymax.bone.service.legislation.core.pojo.LawSearchParams;
import com.baymax.bone.service.legislation.parser.text.IParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public class LawNameParser implements IParser {

    private static Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+[法|法典]$");

    @Override
    public void parse(List<String> words, LawSearchParams lawSearchParams) {
        List<String> lawNames = new ArrayList<>();
        Set<String> wordMatchSet = new HashSet<>();
        words.forEach((word) -> {
            List<String> matchList = parseLawName(word);
            if(!matchList.isEmpty()) {
                lawNames.addAll(matchList);
                wordMatchSet.add(word);
            }
        });

        this.cleanWords(words, wordMatchSet);
        lawSearchParams.setLawName(lawNames);
    }

    public static final List<String> parseLawName(String word) {
        List<String> list = new ArrayList<>(3);
        Matcher matcher = pattern.matcher(word);
        while (matcher.find()) {
            String lawName = matcher.group();
            list.add(lawName);
        }
        return list;
    }
}
