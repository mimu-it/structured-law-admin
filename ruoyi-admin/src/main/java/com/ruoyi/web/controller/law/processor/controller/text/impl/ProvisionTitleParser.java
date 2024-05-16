package com.ruoyi.web.controller.law.processor.controller.text.impl;

import cn.hutool.core.convert.NumberChineseFormatter;
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
public class ProvisionTitleParser implements IParser {

    private static Pattern patternMatchProvisionTitle = Pattern.compile("[零一二三四五六七八九十百千万亿]+条");
    private static Pattern patternMatchProvisionArabicTitle = Pattern.compile("([\\d]+)条$");

    @Override
    public void parse(List<String> words, IntegralParams integralParams) {
        String[] termTitleArray = integralParams.getTermTitleArray();
        if(termTitleArray != null && termTitleArray.length > 0) {
            /** 如果已经指定了值，就不处理 */
            return;
        }

        List<String> provisionTitles = new ArrayList<>();
        Set<String> wordMatchSet = new HashSet<>();

        words.forEach((word) -> {
            String provisionTitle = toChineseProvisionTitle(word);
            List<String> matchList = parseProvisionTitle(provisionTitle);
            if(!matchList.isEmpty()) {
                provisionTitles.addAll(matchList);
                wordMatchSet.add(word);
            }
        });

        this.cleanWords(words, wordMatchSet);
        integralParams.setTermTitleArray(provisionTitles.toArray(new String[0]));
    }

    /**
     * 搜集 第一百二十三条 这样的值
     * @param word
     * @return
     */
    public static final List<String> parseProvisionTitle(String word) {
        List<String> list = new ArrayList<>(3);
        Matcher matcher = patternMatchProvisionTitle.matcher(word);
        while (matcher.find()) {
            String provisionTitle = matcher.group();

            if(provisionTitle.startsWith("一十")) {
                /** 条款是"十七条"， 而不是"一十七条" */
                provisionTitle = provisionTitle.substring(1);
            }

            if(word.startsWith("第")) {
                provisionTitle = "第" + provisionTitle;
            }

            list.add(provisionTitle);
        }
        return list;
    }

    /**
     * 将  第123条 变成第一百二十三条
     * @param provisionTitle
     * @return
     */
    private static final String toChineseProvisionTitle(String provisionTitle) {
        Matcher matcher = patternMatchProvisionArabicTitle.matcher(provisionTitle);
        while (matcher.find()) {
            String number = matcher.group(1);
            provisionTitle = provisionTitle.replace(number, NumberChineseFormatter.format(Long.parseLong(number), false));
            return provisionTitle;
        }
        return provisionTitle;
    }
}
