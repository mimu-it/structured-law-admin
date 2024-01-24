package com.ruoyi.web.controller.law.srv;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-01-05
 * @apiNote
 */
public abstract class AbstractEsSrv {

    /**
     * 获取对应类型的索引配置文件
     * @return
     */
    public abstract String getMappingConfig();

    /**
     * 统计数据总数
     * @return
     */
    public abstract int countData();

    /**
     * 获取对应类型的数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    public abstract Page<IntegralFields> listDataByPage(int pageNum, int pageSize);

    /**
     * 构造查询条件
     * @param condition
     * @return
     */
    public abstract SearchSourceBuilder mustConditions(EsFields condition);

    /**
     *
     * @return
     */
    public String getResourcePathPrefix() {
        String profile = SpringUtils.getActiveProfile();
        return "prod".equals(profile)? "classpath:" : "";
    }

    /**
     * 读取jar包中的配置文件
     * @param filePath
     * @return
     * @throws IOException
     */
    protected String readConfig(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        BufferedReader JarUrlProcReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StringBuilder buffer = new StringBuilder();

        String JarUrlProcStr;
        while((JarUrlProcStr = JarUrlProcReader.readLine()) != null) {
            buffer.append(JarUrlProcStr);
        }

        return buffer.toString();
    }

    /**
     *
     * @param publishRange
     * @param rangeQueryBuilder
     */
    protected void makeRangeQueryBuilder(String[] publishRange, RangeQueryBuilder rangeQueryBuilder) {
        String begin = publishRange[0];
        if (begin != null) {
            rangeQueryBuilder.gte(begin);
        }

        String end = publishRange[1];
        if (end != null) {
            rangeQueryBuilder.lte(end);
        }
    }

    /**
     * 公共条件的设置，属于高级搜索条件，都是并且的关系
     * @param condition
     * @return
     */
    protected BoolQueryBuilder makeCommonBoolQueryBuilder(EsFields condition) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String authority = condition.getAuthority();
        if (StrUtil.isNotBlank(authority)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.AUTHORITY, authority));
        }

        String authorityProvince = condition.getAuthorityProvince();
        if (StrUtil.isNotBlank(authorityProvince)) {
            List<String> provinces = JSONUtil.toList(authorityProvince, String.class);
            boolQueryBuilder.should(QueryBuilders.termsQuery(IntegralFields.AUTHORITY_PROVINCE, provinces));
        }

        String authorityCity = condition.getAuthorityCity();
        if (StrUtil.isNotBlank(authorityCity)) {
            List<String> cities = JSONUtil.toList(authorityCity, String.class);
            boolQueryBuilder.must(QueryBuilders.termsQuery(IntegralFields.AUTHORITY_CITY, cities));
        }

        String authorityDistrict = condition.getAuthorityDistrict();
        if (StrUtil.isNotBlank(authorityDistrict)) {
            List<String> districts = JSONUtil.toList(authorityDistrict, String.class);
            boolQueryBuilder.must(QueryBuilders.termsQuery(IntegralFields.AUTHORITY_DISTRICT, districts));
        }

        String lawLevel = condition.getLawLevel();
        if (StrUtil.isNotBlank(lawLevel)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_LEVEL, lawLevel));
        }

        String documentType = condition.getDocumentType();
        if (StrUtil.isNotBlank(documentType)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.DOCUMENT_TYPE, documentType));
        }

        Integer status = condition.getStatus();
        if (status != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.STATUS, status));
        }

        String publishRangeStr = condition.getPublishRange();
        if(StrUtil.isNotBlank(publishRangeStr)) {
            String[] publishRange = JSONUtil.toList(publishRangeStr, String.class).toArray(new String[0]);
            if (publishRange != null && publishRange.length == 2) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(IntegralFields.PUBLISH).timeZone("GMT+8");
                makeRangeQueryBuilder(publishRange, rangeQueryBuilder);
                boolQueryBuilder.must(rangeQueryBuilder);
            }
        }

        String validFromRangeStr = condition.getValidFromRange();
        if(StrUtil.isNotBlank(validFromRangeStr)) {
            String[] validFromRange = JSONUtil.toList(validFromRangeStr, String.class).toArray(new String[0]);
            if (validFromRange != null && validFromRange.length == 2) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(IntegralFields.VALID_FROM).timeZone("GMT+8");
                makeRangeQueryBuilder(validFromRange, rangeQueryBuilder);
                boolQueryBuilder.must(rangeQueryBuilder);
            }
        }

        String[] documentNoArray = condition.getDocumentNoArray();
        if (ArrayUtil.isNotEmpty(documentNoArray)) {
            BoolQueryBuilder boolQueryBuilderShould = QueryBuilders.boolQuery();
            for(String documentNo : documentNoArray) {
                boolQueryBuilderShould.must(QueryBuilders.wildcardQuery(IntegralFields.DOCUMENT_NO, "*" + documentNo + "*"));
            }

            boolQueryBuilder.must(boolQueryBuilderShould);
        }
        return boolQueryBuilder;
    }
}
