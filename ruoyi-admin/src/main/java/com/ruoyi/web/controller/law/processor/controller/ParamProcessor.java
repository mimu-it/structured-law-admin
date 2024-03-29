package com.ruoyi.web.controller.law.processor.controller;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiao.hu
 * @date 2024-03-02
 * @apiNote
 */
public class ParamProcessor {
    /**
     * 定义正则表达式模式
     */
    private static Pattern pattern = Pattern.compile("\\d+");


    /**
     * 如果大输入框输入了罪名的查询条件，则直接去定位到罪名查询
     * @param contentText
     * @return
     */
    public static final String existsCharges(String contentText) {
        if(StrUtil.isBlank(contentText)) {
            return null;
        }

        String[] keywords = contentText.split(" ");
        for(String keyword : keywords) {
            if(keyword.endsWith("罪")) {
                //integralParams.setTags(Arrays.asList(keyword));
                return keyword;
            }
        }
        return null;
    }

    /**
     * 如果 contentText 不为空，就说明是输入了大输入框的搜索条件
     * 当 lawName 或者 termText 参数无值时，拿这个值做默认值
     */
    public static final void handleBigInput(IntegralParams integralParams, String contentText) {
        if(StrUtil.isNotBlank(contentText)) {
            if(StrUtil.isBlank(integralParams.getLawName())) {
                integralParams.setLawName(contentText);
            }

            if(StrUtil.isBlank(integralParams.getTermText())) {
                integralParams.setTermText(contentText);
            }
        }
    }

    /**
     * 处理条款的序号标题
     * @param integralParams
     * @param termTitleArrayStr
     */
    public static final void handleTermTitle(IntegralParams integralParams, String termTitleArrayStr) {
        if (StrUtil.isNotBlank(termTitleArrayStr)) {
            /** 将"第1条"变成"第一条" */
            Matcher matcher = pattern.matcher(termTitleArrayStr);
            while (matcher.find()) {
                String number = matcher.group();
                termTitleArrayStr = termTitleArrayStr.replace(number, NumberChineseFormatter.format(Long.parseLong(number), false));
            }


            String[] termTitleArray = JSONUtil.toList(termTitleArrayStr, String.class).toArray(new String[0]);
            integralParams.setTermTitleArray(termTitleArray);
        }
    }

    /**
     * 处理效力级别，支持多选
     * @param integralParams
     * @param lawLevelArrayStr
     */
    public static final void handleLawLevel(IntegralParams integralParams, String lawLevelArrayStr) {
        /**
         * 效力级别可以选择多个
         */
        String[] levelMultiStrArray;
        if (StrUtil.isNotBlank(lawLevelArrayStr)) {
            if(JSONUtil.isTypeJSON(lawLevelArrayStr)) {
                /** 是json格式 */
                levelMultiStrArray = JSONUtil.toList(lawLevelArrayStr, String.class).toArray(new String[0]);
                integralParams.setLawLevelArray(levelMultiStrArray);
            }
            else {
                /** 不是json格式 */
                levelMultiStrArray = new String[] { lawLevelArrayStr };
                integralParams.setLawLevelArray(levelMultiStrArray);
            }
        }
    }

    /**
     * 处理发布日期查询范围的条件
     * @param integralParams
     * @param publishRangeStr
     */
    public static final void handlePublishRange(IntegralParams integralParams, String publishRangeStr) {
        if (StrUtil.isNotBlank(publishRangeStr)) {
            String[] publishRangeArray = JSONUtil.toList(publishRangeStr, String.class).toArray(new String[0]);
            integralParams.setPublishRange(publishRangeArray);
        }
    }


    /**
     * 处理实施日期查询范围的条件
     * @param integralParams
     * @param validFromRangeStr
     */
    public static final void handleValidFromRange(IntegralParams integralParams, String validFromRangeStr) {
        if (StrUtil.isNotBlank(validFromRangeStr)) {
            String[] validFromRangeArray = JSONUtil.toList(validFromRangeStr, String.class).toArray(new String[0]);
            integralParams.setValidFromRange(validFromRangeArray);
        }
    }


    /**
     * 处理文号的条件
     * @param integralParams
     * @param documentNoMulti
     */
    public static final void handleDocumentNo(IntegralParams integralParams, String documentNoMulti) {
        if (StrUtil.isNotBlank(documentNoMulti)) {
            String[] documentNoMultiStrArray = JSONUtil.toList(documentNoMulti, String.class).toArray(new String[0]);
            integralParams.setDocumentNoArray(documentNoMultiStrArray);
        }
    }

    /**
     * 处理状态的条件
     * @param integralParams
     * @param statusStr
     */
    public static final void handleStatus(IntegralParams integralParams, String statusStr) {
        if (StrUtil.isNotBlank(statusStr)) {
            Integer[] statusArray = JSONUtil.toList(statusStr, Integer.class).toArray(new Integer[0]);
            integralParams.setStatusArray(statusArray);
        }
    }


    public static final void handleAuthority(IntegralParams integralParams, String authorityStr) {
        if (StrUtil.isNotBlank(authorityStr)) {
            String[] authorityStrArray = JSONUtil.toList(authorityStr, String.class).toArray(new String[0]);
            integralParams.setAuthorityArray(authorityStrArray);
        }
    }

    public static final void handleAuthorityCity(IntegralParams integralParams, String authorityCityStr) {
        if (StrUtil.isNotBlank(authorityCityStr)) {
            String[] authorityCityStrArray = JSONUtil.toList(authorityCityStr, String.class).toArray(new String[0]);
            integralParams.setAuthorityCityArray(authorityCityStrArray);
        }
    }

    public static final void handleAuthorityProvince(IntegralParams integralParams, String authorityProvinceStr) {
        if (StrUtil.isNotBlank(authorityProvinceStr)) {
            String[] authorityProvinceStrStrArray = JSONUtil.toList(authorityProvinceStr, String.class).toArray(new String[0]);
            integralParams.setAuthorityProvinceArray(authorityProvinceStrStrArray);
        }
    }

    /**
     * 公共属性的组装
     * @param authorityDistrict
     * @param integralParams
     */
    public static final void handleGeneric(String lawName,
                                           String termText,
                                           String authorityDistrict,
                                           IntegralParams integralParams) {
        /**
         * 如果指定了 lawName 或者 termText 参数，就以这个参数为准
         */
        if (StrUtil.isNotBlank(lawName)) {
            integralParams.setLawName(lawName);
        }

        /**
         * 如果传入了条款内容
         */
        if (StrUtil.isNotBlank(termText)) {
            integralParams.setTermText(termText);
        }

        if (StrUtil.isNotBlank(authorityDistrict)) {
            integralParams.setAuthorityDistrict(authorityDistrict);
        }
    }
}
