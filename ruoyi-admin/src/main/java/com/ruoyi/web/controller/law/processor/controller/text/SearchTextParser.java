package com.ruoyi.web.controller.law.processor.controller.text;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;
import com.ruoyi.web.controller.law.processor.controller.text.impl.LawNameParser;
import com.ruoyi.web.controller.law.processor.controller.text.impl.NormalTextParser;
import com.ruoyi.web.controller.law.processor.controller.text.impl.ProvisionTitleParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-04-19
 * @apiNote
 */
public class SearchTextParser {

    private static List<IParser> parserChain = new ArrayList<>();
    static {
        parserChain.add(new LawNameParser());
        parserChain.add(new ProvisionTitleParser());
        parserChain.add(new NormalTextParser());
    }

    public static void parse(String text, IntegralParams integralParams) {
        if(StrUtil.isNotBlank(text)) {
            String[] wordsArr = text.split("\\s+");
            List<String> words = new ArrayList<>(Arrays.asList(wordsArr));
            parserChain.forEach((parser) -> parser.parse(words, integralParams));
        }
    }
}
