package com.ruoyi.web.controller.law.processor.controller.text.impl;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;
import com.ruoyi.web.controller.law.processor.controller.text.IParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public void parse(List<String> words, IntegralParams integralParams) {
        if(StrUtil.isNotBlank(integralParams.getLawName())) {
            /** 如果已经指定了法律名称，则忽略 */
            return;
        }

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
        if(lawNames.size() > 0) {
            integralParams.setLawName(lawNames.get(0));
        }
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
