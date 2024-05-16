package com.baymax.bone.service.legislation.parser.text;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baymax.bone.service.legislation.core.pojo.LawSearchParams;
import com.baymax.bone.service.legislation.parser.text.impl.LawNameParser;
import com.baymax.bone.service.legislation.parser.text.impl.NormalTextParser;
import com.baymax.bone.service.legislation.parser.text.impl.ProvisionTitleParser;

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

    public static void parse(String text, LawSearchParams lawSearchParams) {
        if(StrUtil.isNotBlank(text)) {
            String[] wordsArr = text.split("\\s+");
            List<String> words = new ArrayList<>(Arrays.asList(wordsArr));
            parserChain.forEach((parser) -> parser.parse(words, lawSearchParams));
        }
    }
}
