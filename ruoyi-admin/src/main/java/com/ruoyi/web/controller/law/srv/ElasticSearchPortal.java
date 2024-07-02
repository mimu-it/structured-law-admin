package com.ruoyi.web.controller.law.srv;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.pagehelper.Page;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.web.controller.elasticsearch.domain.EsFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralParams;
import com.ruoyi.web.controller.law.api.domain.inner.*;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHits;
import com.ruoyi.web.controller.law.api.domain.resp.LawWithProvisionsMatchedPage;
import com.ruoyi.web.controller.law.api.domain.resp.LawWithProvisionsSearchHits;
import com.ruoyi.web.controller.law.api.domain.resp.SuggestHits;
import com.ruoyi.web.controller.law.cache.LawCache;
import com.ruoyi.web.controller.law.values.LawStatus;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 */
@Service
public class ElasticSearchPortal {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String INDEX__LAW = "law";
    public static final String INDEX__LAW_PROVISION = "law_provision";
    public static final String INDEX__LAW_PROVISION_SPECIFIC = "law_provision_specific";
    public static final String INDEX__LAW_PROVISION_TAGS = "law_provision_tags";
    public static final String INDEX__LAW_ASSOCIATED_FILE = "law_associated_file";

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Resource
    RestHighLevelClient client;

    @Resource
    AbstractEsSrv esLawSrv;

    @Resource
    AbstractEsSrv esLawProvisionSrv;

    @Resource
    AbstractEsSrv esLawProvisionSpecificSrv;

    @Resource
    AbstractEsSrv esLawProvisionTagsSrv;

    @Resource
    AbstractEsSrv esLawAssociatedFileSrv;

    @Resource
    LawCache lawCache;

    Map<String, AbstractEsSrv> srvMap = new HashMap<>();

    @PostConstruct
    public void initSrvMapping() {
        srvMap.put(INDEX__LAW, esLawSrv);
        srvMap.put(INDEX__LAW_PROVISION, esLawProvisionSrv);
        srvMap.put(INDEX__LAW_PROVISION_SPECIFIC, esLawProvisionSpecificSrv);
        srvMap.put(INDEX__LAW_PROVISION_TAGS, esLawProvisionTagsSrv);
        srvMap.put(INDEX__LAW_ASSOCIATED_FILE, esLawAssociatedFileSrv);
    }

    /**
     * 判断索引是否存在
     *
     * @param indexName
     * @return
     */
    public boolean existsIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 初始化所有的索引
     */
    public void initAllIndex() {
        initIndex(INDEX__LAW);
        initIndex(INDEX__LAW_PROVISION);
        initIndex(INDEX__LAW_PROVISION_TAGS);
        initIndex(INDEX__LAW_ASSOCIATED_FILE);
    }

    /**
     * 定义索引
     * <p>
     * PUT /law 接json配置可以生成索引
     * <p>
     * 通过 GET law/_mapping 可以查看索引
     */
    public void initIndex(String indexName) {
        try {
            if (this.existsIndex(indexName)) {
                logger.info("索引已存在");
                return;
            }

            AbstractEsSrv esSrv = srvMap.get(indexName);
            if(esSrv == null) {
                throw new IllegalStateException("No es srv found");
            }

            String mappingsStr = esSrv.getMappingConfig();

            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.mapping(mappingsStr, XContentType.JSON);

            CreateIndexResponse createIndexResponse =
                    client.indices().create(request, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            logger.info("[create index blog :{}]", acknowledged);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 把所有索引都删掉
     * @throws IOException
     */
    public void deleteAllIndex() {
        boolean allSuccess = true;
        try {
            allSuccess = allSuccess && this.deleteIndex(INDEX__LAW);
        } catch (Exception e) {
            logger.error("delete law index failed");
        }

        try {
            allSuccess = allSuccess && this.deleteIndex(INDEX__LAW_PROVISION);
        } catch (Exception e) {
            logger.error("delete law provision index failed");
        }

        try {
            allSuccess = allSuccess && this.deleteIndex(INDEX__LAW_PROVISION_TAGS);
        } catch (Exception e) {
            logger.error("delete law provision tags index failed");
        }

        try {
            allSuccess = allSuccess && this.deleteIndex(INDEX__LAW_ASSOCIATED_FILE);
        } catch (Exception e) {
            logger.error("delete law associated file index failed");
        }

        if(!allSuccess) {
            logger.error("delete all index not all success");
        }
    }

    /**
     * 删除索引
     * @throws IOException
     */
    public boolean deleteIndex(String indexName) {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        try {
            AcknowledgedResponse response = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 自动提示
     * http://localhost:8080/structured-law/portal/suggest?field=law_name&text=婚姻
     *
     * includeFields 包含的内容
     * excludeFields 控制显示内容 (优化查询效率将所有无关查询提示字段都不显示)
     * @return
     */
    public List<SuggestHits> suggest(String indexName, String suggestField, String suggestValue, boolean isPhraseSuggest, String[] includeFields, String[] excludeFields) {
        String suggestFieldDotSuggest = suggestField + ".suggest";
        String suggestionName = suggestFieldDotSuggest + "_suggest";

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        SuggestionBuilder<?> suggestionBuilder;
        if(isPhraseSuggest) {
            return this.phraseSuggest(indexName, suggestField, suggestValue, includeFields);
        }

        // 构建completionSuggestionBuilder传入查询的参数
        /** Completion Suggester：它主要针对的应用场景就是"Auto Completion"，FST数据结构，类似Trie树，不用打开倒排，快速返回，前缀匹配 */
        suggestionBuilder = SuggestBuilders.completionSuggestion(suggestFieldDotSuggest)
                .skipDuplicates(true).prefix(suggestValue).size(10);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        // 定义查询的suggest名称
        suggestBuilder.addSuggestion(suggestionName, suggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);

        /** 构建SearchRequest 指定查询的库 */
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);

        // 执行查询
        SearchResponse searchResponse;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }

        // 获取查询的结果
        Suggest suggest = searchResponse.getSuggest();

        Set<SuggestHits> suggestSet = new HashSet<>();
        int maxSuggest = 0;
        if (suggest != null) {
            // 获取Suggestion的结果
            Suggest.Suggestion result = suggest.getSuggestion(suggestionName);
            // 遍历获得查询结果的Text
            for (Object term : result.getEntries()) {
                if (term instanceof CompletionSuggestion.Entry) {
                    CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;
                    if (!item.getOptions().isEmpty()) {
                        // 若item的option不为空,循环遍历
                        for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                            SuggestHits suggestHits = new SuggestHits();
                            suggestHits.setText(option.getText().toString());
                            suggestHits.setExtraData(option.getHit().getSourceAsMap());

                            Map<String, Object> extraData = suggestHits.getExtraData();
                            Long provisionId = Long.parseLong(StrUtil.toString(extraData.get(IntegralFields.PROVISION_ID)));
                            if(provisionId != null) {
                                IntegralFields row = this.getByProvisionId(provisionId, new String[]{IntegralFields.TITLE_NUMBER});
                                if(row != null) {
                                    extraData.put(IntegralFields.TITLE_NUMBER, row.getTitleNumber());
                                }
                            }

                            if (!suggestSet.contains(suggestHits)) {
                                suggestSet.add(suggestHits);
                                ++maxSuggest;
                            }
                        }
                    }
                }
                if (maxSuggest >= 10) {
                    break;
                }
            }
        }

        return Arrays.asList(suggestSet.toArray(new SuggestHits[]{}));
    }

    /**
     * 直接用分词查询模拟自动提示
     * 返回的建议 text 可能一样，但是对应的文章不一样
     * @return
     */
    private List<SuggestHits> phraseSuggest(String indexName, String suggestField, String text, String[] includeFields) {
        /** 去哪里查索引 */
        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(indexName.equals(ElasticSearchPortal.INDEX__LAW_PROVISION)) {
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.STATUS, LawStatus.effective.getKey()));
        }

        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(suggestField, text));

        searchSourceBuilder.query(boolQueryBuilder);

        List<IntegralFields> list = this.searchToList(20, includeFields,
                new String[]{ IntegralFields.TAG},
                searchRequest, searchSourceBuilder);

        Set<SuggestHits> suggestSet = new HashSet<>();
        for(IntegralFields integralFields : list) {
            SuggestHits suggestHits = new SuggestHits();
            suggestHits.setText(integralFields.getTag());

            Map<String, Object> extraData = new HashMap<>(includeFields.length);

            Map<String, Object> objectMap = BeanUtil.beanToMap(integralFields, true, true);
            for(String key : includeFields) {
                extraData.put(key, objectMap.get(key));
            }

            /** 为了只筛选出有效的值 */
            Long provisionId = Long.parseLong(StrUtil.toString(extraData.get(IntegralFields.PROVISION_ID)));
            if(provisionId == null) {
                throw new IllegalArgumentException("we should do search include provision id");
            }

            IntegralFields matchOne = this.getByProvisionId(provisionId, new String[]{
                    IntegralFields.STATUS,
                    IntegralFields.TITLE_NUMBER
            });

            if(matchOne.getStatus().intValue() != LawStatus.effective.getKey().intValue()) {
                /** 非有效值就忽略 */
                continue;
            }

            if(StrUtil.isNotBlank(matchOne.getTitleNumber())) {
                extraData.put(IntegralFields.TITLE_NUMBER, matchOne.getTitleNumber());
            }
            suggestHits.setExtraData(extraData);

            suggestSet.add(suggestHits);
        }

        return Arrays.asList(suggestSet.toArray(new SuggestHits[]{}));
    }

    /**
     * 新建索引
     */
    public String insertDataToIndex(String indexName, IntegralFields integralProvision) {
        IndexRequest request = new IndexRequest();
        request.index(indexName);
        if (integralProvision.getProvisionId() != null) {
            request.id(String.valueOf(integralProvision.getProvisionId()));
        }
        try {
            //提供java 对象的 json str
            String dataJsonStr = mapper.writeValueAsString(integralProvision);

            request.source(dataJsonStr, XContentType.JSON);
            IndexResponse index = client.index(request, RequestOptions.DEFAULT);
            return index.getId();
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 把所有的索引都导入数据
     */
    public void importDataToAllIndex() {
        bulkInsert(INDEX__LAW_PROVISION_TAGS);
        bulkInsert(INDEX__LAW_ASSOCIATED_FILE);
        bulkInsert(INDEX__LAW);
        bulkInsert(INDEX__LAW_PROVISION);
    }

    /**
     * 给单个索引导入数据
     * @param indexName
     */
    public void bulkInsert(String indexName) {
        String active = SpringUtils.getActiveProfile();
        boolean isProd = Constants.PROD.equals(active);

        AbstractEsSrv esSrv = srvMap.get(indexName);
        if(esSrv == null) {
            throw new IllegalStateException("No es srv found");
        }

        int pageNum = 1;
        int pageSize = isProd? 500 : 50;
        //int pageSize = 1000;
        int totalCount = esSrv.countData();
        int totalPage = (totalCount + (pageSize - 1)) / pageSize;

        Page<IntegralFields> page;
        /**
         * 分页往 elasticsearch 中插入数据
         */
        while (pageNum <= totalPage) {
            /** 查询当前页的数据 */
            page = esSrv.listDataByPage(pageNum, pageSize);
            if(!page.getResult().isEmpty()) {
                /** 如果有数据，就批量插入 elasticsearch 中 */
                try {
                    List<IntegralFields> rowList = page.getResult();
                    this.bulkInsert(indexName, rowList);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            pageNum++;
        }

        logger.info(String.format("Import %s data completed, profile: %s", indexName, active));
    }


    /**
     * 批量新增
     *
     * @throws IOException
     */
    public void bulkInsert(String indexName, List<IntegralFields> dataList) {
        if (dataList.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();
        BulkResponse response;
        try {
            //最大数量不得超过20万
            for (IntegralFields item : dataList) {
                String json = mapper.writeValueAsString(item);
                IndexRequest request = new IndexRequest(indexName);
                /** 避免request.id(item.getEsDocId()); 可以成功指定  _id, 这样就可以达成存在并替换，已手工测试成功 */
                request.id(item.getEsDocId());
                /** DocWriteRequest.OpType.INDEX 意味着存在并替换 */
                request.source(json, XContentType.JSON).opType(DocWriteRequest.OpType.INDEX);
                bulkRequest.add(request);
            }

            response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }

        if (response.hasFailures()) {
            String errorMessage = null;
            StringBuilder sb = new StringBuilder();
            for (BulkItemResponse item : response.getItems()) {
                sb.append(", ").append(item.getFailureMessage());
            }

            if (sb.length() > 0) {
                errorMessage = sb.substring(1);
            }

            throw new IllegalStateException(errorMessage);
        }
    }


    /**
     * 通过id查询
     *
     * @throws IOException
     */
    public IntegralFields getByProvisionId(long provisionId, String[] fields) {
        GetRequest request = new GetRequest(INDEX__LAW_PROVISION, StrUtil.toString(provisionId));
        if (ArrayUtil.isNotEmpty(fields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            request.fetchSourceContext(new FetchSourceContext(true, fields, Strings.EMPTY_ARRAY));
        }

        try {
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            String mapStr = JSONUtil.toJsonStr(response.getSource());
            IntegralFields integralFields = JSONUtil.toBean(mapStr, IntegralFields.class);

            if(integralFields.getStatus() != null) {
                integralFields.setStatusLabel(lawCache.getStatusOptionsMap().get(integralFields.getStatus()));
            }

            return integralFields;
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 查看历史，只查法律
     * @param lawName
     * @param size
     * @param fields
     * @return
     */
    public List<IntegralFields> listLawHistory(String lawName, Integer size, String[] fields) {
        SearchRequest searchRequest = new SearchRequest(INDEX__LAW);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /*
        searchSourceBuilder.query(
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(IntegralFields.LAW_NAME, lawName))
        );*/
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.wildcardQuery(IntegralFields.LAW_NAME + ".keyword", lawName + "*"));
        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort(IntegralFields.PUBLISH, SortOrder.DESC);
        return this.searchToList(size, fields,
                new String[]{ IntegralFields.LAW_NAME },
                searchRequest, searchSourceBuilder);
    }


    /**
     * 获取索引中的原始全文，未进行结构化拆分条款的全文
     * @param lawId
     * @return
     */
    public String getLawFullContent(long lawId) {
        SearchRequest searchRequest = new SearchRequest(INDEX__LAW);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_ID, lawId));
        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort(IntegralFields.PUBLISH, SortOrder.DESC);
        List<IntegralFields> list = this.searchToList(1, new String[]{ IntegralFields.FULL_CONTENT },
                new String[]{ IntegralFields.FULL_CONTENT },
                searchRequest, searchSourceBuilder);
        if(list == null || list.isEmpty()) {
            return "";
        }

        IntegralFields fields = list.get(0);
        return fields.getFullContent();
    }


    /**
     * 查看历史，查法律具体条款
     * @param lawName
     * @param size
     * @param fields
     * @return
     */
    public List<IntegralFields> listLawProvisionsHistory(String lawName, String title, Integer size, String[] fields) {
        SearchRequest searchRequest = new SearchRequest(INDEX__LAW_PROVISION);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /** 查历史，需精确匹配法律名，不然会导致无关法律也被查出来 */
        boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_NAME + ".keyword", lawName));
        if(StrUtil.isNotBlank(title)) {
            /** 查历史，条目标题如果能最大精确，那就应该选择最大精确匹配，至少也应该是短语匹配 */
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.TITLE + ".keyword", title));
        }

        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort(IntegralFields.PUBLISH, SortOrder.DESC);
        return this.searchToList(size, fields,
                new String[]{ IntegralFields.TERM_TEXT, IntegralFields.LAW_NAME },
                searchRequest, searchSourceBuilder);
    }

    /**
     * 一次批量查询对应的历史
     * @param lawProvisionList
     * @param size
     * @param fields
     * @return
     */
    public List<IntegralFields> listLawProvisionsHistoryByBat(Set<LawProvision> lawProvisionList, Integer size, String[] fields, BoolQueryBuilder existingQueryForHistory) {
        SearchRequest searchRequest = new SearchRequest(INDEX__LAW_PROVISION);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder batBoolQueryBuilder = QueryBuilders.boolQuery();
        lawProvisionList.forEach((item) -> {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            /** 查历史，需精确匹配法律名，不然会导致无关法律也被查出来 */
            boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_NAME + ".keyword", item.getLawName()));
            if(StrUtil.isNotBlank(item.getTermTitle())) {
                /** 查历史，条目标题如果能最大精确，那就应该选择最大精确匹配，至少也应该是短语匹配 */
                boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.TITLE + ".keyword", item.getTermTitle()));
            }
            boolQueryBuilder.must(existingQueryForHistory);

            batBoolQueryBuilder.should(boolQueryBuilder);
        });

        searchSourceBuilder.query(batBoolQueryBuilder);

        searchSourceBuilder.sort(IntegralFields.PUBLISH, SortOrder.DESC);
        return this.searchToList(size, fields,
                new String[]{ IntegralFields.TERM_TEXT, IntegralFields.LAW_NAME },
                searchRequest, searchSourceBuilder);
    }



    /**
     * 通过id查询
     * http://localhost:8080/structured-law/portal/law-content?law_id=110
     *
     * @throws IOException
     */
    public List<IntegralFields> listProvisionsByLawId(long lawId, String title, Integer size, String[] fields) {
        SearchRequest searchRequest = new SearchRequest(INDEX__LAW_PROVISION);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /** termQuery 不会分词，最小单位匹配， 不能用于全匹配 */
        boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_ID, lawId));

        if(StrUtil.isNotBlank(title)) {
            /** 如果条目小标题不为空，则需要加入搜索条件中 */
            boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(IntegralFields.TITLE, title));
        }

        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort(IntegralFields.TITLE_NUMBER + ".keyword", SortOrder.ASC);
        return this.searchToList(size, fields,
                new String[]{ IntegralFields.TERM_TEXT, IntegralFields.LAW_NAME },
                searchRequest, searchSourceBuilder);
    }

    /**
     * 执行查询
     * @param size
     * @param fields
     * @param searchRequest
     * @param searchSourceBuilder
     * @return
     */
    private List<IntegralFields> searchToList(Integer size, String[] fields, String[] highlightFields, SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder) {
        if (ArrayUtil.isNotEmpty(fields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, fields, Strings.EMPTY_ARRAY));
        }

        if (size != null) {
            searchSourceBuilder.size(size);
        }

        SearchResponse response = this.doEsSearch(highlightFields, searchSourceBuilder, searchRequest);

        if (response.status().getStatus() == 200) {
            ArrayList<IntegralFields> list = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                this.mixHighlight(highlightFields, list, hit);
            }
            return list;
        }

        return null;
    }

    /**
     * 不同的页码，得到不同的命中数据
     *
     * @param pageNum
     * @return
     * @throws IOException
     */
    public LawSearchHits searchByPage(String indexName, int pageNum, int pageSize, String[] specificFields, String[] highlightFields,
                                      String sortField, Boolean sortType, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest request = new SearchRequest(indexName);
        if (ArrayUtil.isNotEmpty(specificFields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, specificFields, Strings.EMPTY_ARRAY));
        }

        /** from 是从 0 开始的， 设置确定结果要从哪个索引开始搜索的from选项，默认为0 */
        int from = pageNum - 1;
        from = from <= 0 ? 0 : from * pageSize;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);

        // 根据得分降序排序
        searchSourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));

        /**
         * 设置排序
         */
        if (StrUtil.isNotBlank(sortField)) {
            if(sortType == null) {
                sortType = true;
            }

            if(!IntegralFields.PUBLISH.equals(sortField) && !IntegralFields.VALID_FROM.equals(sortField)) {
                sortField = sortField + ".keyword";
            }

            //排序字段，注意如果proposal_no是text类型会默认带有keyword性质，需要拼接.keyword
            searchSourceBuilder.sort(sortField, sortType ? SortOrder.ASC : SortOrder.DESC);
        }

        SearchResponse response = this.doEsSearch(highlightFields, searchSourceBuilder, request);
        if (response.status().getStatus() == 200) {
            // 解析对象
            LawSearchHits lawSearchHits = remakeSearchResponse(response, highlightFields);
            lawSearchHits.setPageNum(pageNum);
            lawSearchHits.setPageSize(pageSize);
            return lawSearchHits;
        }
        return null;
    }


    /**
     * 不同的页码，得到不同的命中数据
     *
     * elasticSearch7.7 不支持聚合查询分页
     *
     * @param pageNum
     * @return
     * @throws IOException
     */
    public List<LawWithProvisionsSearchHits> searchProvisionAggregationsDistinctByLawId(int pageNum, int pageSize, String[] fields, String[] highlightFields,
                                                                                        String sortField, Boolean sortType, IntegralParams integralParams) {
        /** 构造es查询条件 */
        SearchSourceBuilder searchSourceBuilder = this.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralParams);
        /** 至少保证3部法律 */
        //定义avg聚合，指定字段为法律ID
        String aggregationTermsName = "distinct_law";
        String aggregationOfCountMatch = "count_match";
        if(StrUtil.isBlank(sortField)) {
            sortField = IntegralFields.PUBLISH;
        }

        if(sortType == null) {
            sortType = false;
        }

        /**
         * 统计某个字段的数量
         * AggregationBuilders.count("count_uid").field("uid");
         *
         * 去重统计某个字段的数量（有少量误差）
         * CardinalityBuilder cb = AggregationBuilders.cardinality("distinct_count_uid").field("uid");
         *
         * 求最大值
         * MaxBuilder mb= AggregationBuilders.max("max_price").field("price");
         *
         * 求最小值
         * MinBuilder min= AggregationBuilders.min("min_price").field("price");
         *
         *
         */
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(aggregationTermsName).field(IntegralFields.LAW_ID)
                //告诉引擎我需要按名称为 aggregationOfCountMatch 的方案排序， subAggregation 介绍了aggregationOfCountMatch的排序是如何定义的
                //.order(BucketOrder.aggregation(aggregationOfCountMatch, sortType))
                .order(BucketOrder.compound(
                        /**
                         * in order of priority:
                         * sort by sub-aggregation first
                         * 比如查询"刑事 诉讼"，注意带上状态，这样不会引发失效的诉讼匹配记录多于"刑事"，而排到前面
                         */
                        BucketOrder.aggregation(aggregationOfCountMatch, false),
                        BucketOrder.aggregation("publish_sort", false)
                        )
                )
                .subAggregation(
                        //AggregationBuilders.count(aggregationOfCountMatch).field(IntegralFields.LAW_NAME + ".keyword")
                        AggregationBuilders.count(aggregationOfCountMatch).field(IntegralFields.LAW_NAME + ".keyword")
                        //不能按发布时间排序AggregationBuilders.max(aggregationOfCountMatch).field(sortField)
                ).subAggregation(
                        AggregationBuilders.max("publish_sort").field(IntegralFields.PUBLISH)
                ).size(3);

        //添加聚合
        searchSourceBuilder.aggregation(aggregationBuilder);

        SearchRequest request = new SearchRequest(ElasticSearchPortal.INDEX__LAW_PROVISION);
        if (ArrayUtil.isNotEmpty(fields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, fields, Strings.EMPTY_ARRAY));
        }

        /**
         * 聚合不需要设置分页，所以直接设成0
         */
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);

        // 根据得分降序排序
        searchSourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));

        if (StrUtil.isNotBlank(sortField)) {
            /**
             * 如果存在排序字段
             */
            if(sortType == null) {
                sortType = true;
            }

            if(!IntegralFields.PUBLISH.equals(sortField) && !IntegralFields.VALID_FROM.equals(sortField)) {
                /** 排序字段跟时间无关的情况下，需要使用 .keyword */
                sortField = sortField + ".keyword";
            }

            //排序字段，注意如果proposal_no是text类型会默认带有keyword性质，需要拼接.keyword
            searchSourceBuilder.sort(sortField, sortType ? SortOrder.ASC : SortOrder.DESC);
        }

        /**
         * 把组合的聚合查询条件传入es进行查询
         */
        SearchResponse response = this.doEsSearch(highlightFields, searchSourceBuilder, request);

        List<LawWithProvisionsSearchHits> lawWithProvisionsSearchHitsList = new ArrayList<>();
        if (response.status().getStatus() == 200) {
            /** 获取聚合结果 */
            Aggregations aggregations = response.getAggregations();
            Terms groupAggregation = aggregations.get(aggregationTermsName);

            /**
             * 从分组中再分别查询对应法律的条目内容
             */
            logger.debug("================ Aggregation ==================");
            for (Terms.Bucket bucket : groupAggregation.getBuckets()) {
                // 分组的键
                String lawIdStr = bucket.getKeyAsString();
                logger.debug("lawIdStr:" + lawIdStr);
                // 分组中的文档数量
                long docCount = bucket.getDocCount();
                // 处理分组结果
                //logger.debug("{}, {}", key, docCount);

                /** from 是从 0 开始的， 设置确定结果要从哪个索引开始搜索的from选项，默认为0 */
                int from = pageNum - 1;
                from = from <= 0 ? 0 : from * pageSize;
                searchSourceBuilder.from(from);
                searchSourceBuilder.size(pageSize);


                /** 复制一个 searchSourceBuilder， 并获取它的查询条件 */
                SearchSourceBuilder clonedBuilder = cloneSearchSourceBuilder(searchSourceBuilder);
                BoolQueryBuilder existingQuery = (BoolQueryBuilder) clonedBuilder.query();

                /** 用同样的条件增加法律id的查询条件再进行查询 */
                existingQuery.must(QueryBuilders.termQuery(IntegralFields.LAW_ID, lawIdStr));
                /** 第4个参数：null 就会查询所有值， 第5个参数：高亮这些值*/
                LawSearchHits lawSearchHits = this.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION, pageNum, pageSize, null,
                        new String[]{ IntegralFields.LAW_NAME, IntegralFields.TERM_TEXT, IntegralFields.TITLE }, IntegralFields.TITLE_NUMBER, true, clonedBuilder);

                /** 从查询结果中获取法律名称，并继续从es中获取历史 */
                List<LawWithProvisionsMatched> lawWithProvisionsMatched = this.makeLawWithProvisionsMatchedByBat(lawSearchHits.getSearchHits(), QueryBuilders.boolQuery());

                /** 从查询结果中获取法律名称，并继续从es中获取关联文件 */
                Map<Long, List<IntegralFields>> associatedFileMap = this.makeAssociatedFiles(lawSearchHits);

                LawWithProvisionsSearchHits lawWithProvisionsSearchHits = new LawWithProvisionsSearchHits();
                //lawWithProvisionsSearchHits.setLawSearchHits(lawSearchHits);
                lawWithProvisionsSearchHits.setLawWithProvisionsMatched(lawWithProvisionsMatched);
                lawWithProvisionsSearchHits.setAssociatedFileMap(associatedFileMap);

                lawWithProvisionsSearchHitsList.add(lawWithProvisionsSearchHits);
            }


            return lawWithProvisionsSearchHitsList;
        }
        return null;
    }

    /**
     * 分页加载，但没有总页数
     *
     * http://localhost:8080/structured-law/portal/category/search-law?page_num=1&content_text=婚姻&law_level=地方性法规
     *
     * @param pageNum
     * @param pageSize
     * @param integralParams
     * @return
     */
    public LawWithProvisionsMatchedPage searchProvisionDistinctByLawIdInPage(int pageNum, int pageSize, IntegralParams integralParams) {
        /** 构造es查询条件,  这个条件会被多次复制 */
        SearchSourceBuilder searchSourceBuilder = this.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralParams);
        /** 复制原始条件 */
        SearchSourceBuilder clonedBuilder = cloneSearchSourceBuilder(searchSourceBuilder);

        /** 桶分页 */
        int from = (pageNum - 1) * pageSize;
        /** 因为我们只关注桶内容，所以这里全部设为0 */
        clonedBuilder.from(0);
        clonedBuilder.size(0);

        /** 设置聚合 */
        final String aggregationName = "my_aggregation";
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(aggregationName)
                .field(IntegralFields.LAW_ID)
                /**
                 * 如果我只查询第一页，那么只需要10条， 如果我要查询第二页，那么需要20条，并在桶中跳过前10条
                 * 越到后面页，性能应该越差
                 */
                .size(pageNum * pageSize)
                /**
                 * 如果id是数值类型，并且你想要根据id的大小进行排序，你可以使用BucketOrder.key(boolean)，
                 * 其中boolean参数表示排序是升序（true）还是降序（false）。
                 */
                .order(BucketOrder.key(true)).subAggregation(
                        new BucketSortPipelineAggregationBuilder("sorted_categories", new ArrayList<>()).from(from).size(pageSize));
        clonedBuilder.aggregation(aggregation);

        /** 开始查询 */
        SearchRequest searchRequest = new SearchRequest(ElasticSearchPortal.INDEX__LAW_PROVISION);
        searchRequest.source(clonedBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }

        /** 处理聚合结果 */
        List<LawWithProvisionsMatched> lawWithProvisionsMatchedList = new ArrayList<>();
        Terms terms = searchResponse.getAggregations().get(aggregationName);
        for (Terms.Bucket entry : terms.getBuckets()) {
            Long lawId = (Long) entry.getKey();
            long docCount = entry.getDocCount();
            // 处理每个聚合桶的数据...
            //logger.debug("lawId: {}, docCount: {}", lawId, docCount);

            SearchSourceBuilder clonedBuilderForSpecificLawId = cloneSearchSourceBuilder(searchSourceBuilder);
            BoolQueryBuilder existingQuery = (BoolQueryBuilder) clonedBuilderForSpecificLawId.query();

            /** 用同样的条件增加法律id的查询条件再进行查询 */
            existingQuery.must(QueryBuilders.termQuery(IntegralFields.LAW_ID, lawId));
            /** 第4个参数：null 就会查询所有值， 第5个参数：高亮这些值*/
            LawSearchHits lawSearchHits = this.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION, 1, 10000, null,
                    new String[]{ IntegralFields.LAW_NAME, IntegralFields.TERM_TEXT, IntegralFields.TITLE }, IntegralFields.TITLE_NUMBER, true, clonedBuilderForSpecificLawId);

            /**
             * 构建返回对象
             */
            LawWithProvisionsMatched lawWithProvisionsMatched = new LawWithProvisionsMatched();

            List<IntegralFields> matchedList = lawSearchHits.getSearchHits();
            lawWithProvisionsMatched.setProvisionList(matchedList);
            if(!matchedList.isEmpty()) {
                IntegralFields row = matchedList.get(0);
                lawWithProvisionsMatched.setLawName(row.getLawName());
                lawWithProvisionsMatched.setLawNameOrigin(row.getLawNameOrigin());
                lawWithProvisionsMatched.setAuthority(row.getAuthority());
                lawWithProvisionsMatched.setAuthorityProvince(row.getAuthorityProvince());
                lawWithProvisionsMatched.setAuthorityCity(row.getAuthorityCity());
                lawWithProvisionsMatched.setAuthorityDistrict(row.getAuthorityDistrict());
                lawWithProvisionsMatched.setDocumentNo(row.getDocumentNo());
                lawWithProvisionsMatched.setLevel(row.getLawLevel());
                lawWithProvisionsMatched.setPublish(row.getPublish());
                lawWithProvisionsMatched.setStatus(row.getStatus());
                lawWithProvisionsMatched.setStatusLabel(row.getStatusLabel());
                lawWithProvisionsMatched.setValidFrom(row.getValidFrom());
            }

            //logger.debug("matchedList: {}", matchedList);
            lawWithProvisionsMatchedList.add(lawWithProvisionsMatched);
        }


        /** 统计去重 , 用于计算某个字段的唯一值数量，也就是去重后的总数 */
        /** 复制原始条件 */
        SearchSourceBuilder clonedBuilderForCardinality = cloneSearchSourceBuilder(searchSourceBuilder);
        final String distinctAggregationName = "distinct_law_id";
        CardinalityAggregationBuilder cardinalityAggregationBuilder = AggregationBuilders.cardinality(distinctAggregationName).field(IntegralFields.LAW_ID);
        clonedBuilderForCardinality.aggregation(cardinalityAggregationBuilder);
        clonedBuilderForCardinality.size(0);
        searchRequest.source(clonedBuilderForCardinality);

        long size = 0;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null != searchResponse) {
                // 这个field就是上面起的别名
                ParsedCardinality parsedCardinality = searchResponse.getAggregations().get(distinctAggregationName);
                size = parsedCardinality.getValue();
            }
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }

        LawWithProvisionsMatchedPage page = new LawWithProvisionsMatchedPage();
        page.setList(lawWithProvisionsMatchedList);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setTotal(size);
        page.setTotalPage((int) ((size - 1) / pageSize + 1));
        logger.debug("size: {}", size);

        return page;
    }



    /**
     * clone SearchSourceBuilder
     * @param old
     * @return
     */
    private SearchSourceBuilder cloneSearchSourceBuilder(SearchSourceBuilder old) {
        // 将 SearchSourceBuilder 转换为 JSON 字符串
        String json = old.toString();

        SearchSourceBuilder clonedBuilder = new SearchSourceBuilder();
        final SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
        final XContentParser fullQueryJsonParser;
        try {
            fullQueryJsonParser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), LoggingDeprecationHandler.INSTANCE, json);
            clonedBuilder.parseXContent(fullQueryJsonParser);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return clonedBuilder;
    }

    /**
     *
     * @param highlightFields
     * @param searchSourceBuilder
     * @param request
     * @return
     */
    private SearchResponse doEsSearch(String[] highlightFields, SearchSourceBuilder searchSourceBuilder, SearchRequest request) {
        //高亮
        HighlightBuilder highlight = new HighlightBuilder();
        if (highlightFields != null) {
            for (String field : highlightFields) {
                highlight.field(field);
            }
        }

        //关闭多个高亮
        //highlight.requireFieldMatch(false);
        highlight.preTags("<span style='color:red'>");
        highlight.postTags("</span>");

        /**
         * 我们希望搜索title字段时，除了title字段中匹配关键字高亮，摘要abstract字段对应的关键字也要高亮，这需要对require_field_match属性进行设置。
         * 默认情况下，只有包含查询匹配的字段才会突出显示，因为默认require_field_match值为true，可以设置为false以突出显示所有字段。
         * title和abstract字段高亮
         */
        highlight.requireFieldMatch(true);
        /**
         * 对一个内容长度比较长的字段进行搜索并使用高亮显示插件时，通过获得结果中的高亮字段获取的内容只有一部分，而非全部内容
         * 当需要获取全部内容时，只需要设置 number_of_fragments 为0 即可返回完整内容
         */
        highlight.numOfFragments(0);

        searchSourceBuilder.highlighter(highlight);
        //不返回源数据。只有条数之类的数据。
        //builder.fetchSource(false);

        searchSourceBuilder.trackTotalHits(true);
        request.source(searchSourceBuilder);

        SearchResponse response;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        return response;
    }


    /**
     * 高亮结果集 特殊处理
     * map转对象 JSONObject.parseObject(JSONObject.toJSONString(map), Content.class)
     *
     * @param searchResponse
     * @param highlightFields
     */
    private LawSearchHits remakeSearchResponse(SearchResponse searchResponse, String[] highlightFields) {
        LawSearchHits lawSearchHits = new LawSearchHits();

        long totalHitsCount = searchResponse.getHits().getTotalHits().value;

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        //解析结果
        ArrayList<IntegralFields> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            mixHighlight(highlightFields, list, hit);
        }

        lawSearchHits.setTotal(totalHitsCount);
        lawSearchHits.setSearchHits(list);
        return lawSearchHits;
    }

    /**
     * 把高亮字段复制给原数据
     * @param highlightFields
     * @param list
     * @param hit
     */
    private void mixHighlight(String[] highlightFields, ArrayList<IntegralFields> list, SearchHit hit) {
        Map<String, HighlightField> high = hit.getHighlightFields();

        //原来的结果
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();

        String lawNameOrigin = (String) sourceAsMap.get(IntegralFields.LAW_NAME);
        String titleOrigin = (String) sourceAsMap.get(IntegralFields.TITLE);

        if (highlightFields != null && high != null) {
            for (String highlightField : highlightFields) {
                HighlightField field = high.get(highlightField);

                //解析高亮字段,将原来的字段换为高亮字段
                if (field != null) {
                    Text[] texts = field.fragments();
                    String nTitle = "";
                    for (Text text : texts) {
                        nTitle += text;
                    }
                    //替换
                    sourceAsMap.put(highlightField, nTitle);
                }
            }
        }

        String mapStr = JSONUtil.toJsonStr(sourceAsMap);
        IntegralFields integralFields = JSONUtil.toBean(mapStr, IntegralFields.class);
        integralFields.setLawNameOrigin(lawNameOrigin);
        integralFields.setTitleOrigin(titleOrigin);

        if(integralFields.getStatus() != null) {
            integralFields.setStatusLabel(lawCache.getStatusOptionsMap().get(integralFields.getStatus()));
        }

        list.add(integralFields);
    }

    /**
     * 在es中进行count, 统计的是条款
     * @param indexName
     * @param searchSourceBuilder
     * @return
     */
    private long countInEs(String indexName, SearchSourceBuilder searchSourceBuilder) {
        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();

        CountRequest countRequest = new CountRequest(indexName);
        countRequest.query(boolQueryBuilder);
        CountResponse countResponse;
        try {
            countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        return countResponse.getCount();
    }

    /**
     * 对每个状态进行分开统计总匹配数
     * @param indexName
     * @param integralParams
     * @return
     */
    public List<StatisticsRecord> countGroupByStatus(String indexName, IntegralParams integralParams) {
        IntegralParams copy = BeanUtil.toBean(integralParams, IntegralParams.class);

        List<StatisticsRecord> resultList = new ArrayList<>();

        Integer[] statusArray = copy.getStatusArray();
        if(statusArray == null || statusArray.length == 0) {
            List<Integer> status = new ArrayList<>();
            Map<Integer, String> statusTypesMap = lawCache.getStatusOptionsMap();
            statusTypesMap.forEach((k, v) -> status.add(k));
            statusArray = status.toArray(new Integer[0]);
        }

        for(Integer status : statusArray) {
            copy.setStatusArray(new Integer[]{ status });
            SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, copy);

            //long totalCount = this.countInEs(indexName, searchSourceBuilder);
            long totalCount = this.countGroupDistinct(indexName, searchSourceBuilder, IntegralFields.LAW_ID);

            if(totalCount > 0) {
                StatisticsRecord statisticsRecord = new StatisticsRecord();
                statisticsRecord.setName(lawCache.getStatusOptionsMap().get(status));
                statisticsRecord.setTotal(totalCount);
                resultList.add(statisticsRecord);
            }
        }

        return resultList;
    }


    /**
     * 查询的数据可用 distinct 去重
     * @param indexName
     * @param field
     * @param paramsArray
     * @param distinctField
     * @param integralParams
     * @return
     */
    private Integer countGroupDistinct(String indexName, String field, String[] paramsArray, String distinctField, IntegralParams integralParams) {
        SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, integralParams);

        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();
        boolQueryBuilder.must(QueryBuilders.termsQuery(field, paramsArray));
        searchSourceBuilder.query(boolQueryBuilder);

        return this.countGroupDistinct(indexName, searchSourceBuilder, distinctField);
    }

    /**
     * 查询的数据可用 distinct 去重
     * @param indexName
     * @param searchSourceBuilder
     * @param distinctField
     * @return
     */
    private Integer countGroupDistinct(String indexName, SearchSourceBuilder searchSourceBuilder, String distinctField) {
        // 创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 创建查询资源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();
        sourceBuilder.query(boolQueryBuilder);

        /** 统计去重 , 用于计算某个字段的唯一值数量，也就是去重后的总数 */
        CardinalityAggregationBuilder cardinalityAggregationBuilder = AggregationBuilders.cardinality(distinctField).field(distinctField);
        sourceBuilder.aggregation(cardinalityAggregationBuilder);
        sourceBuilder.size(0);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse;
        int size;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null == searchResponse) {
                return 0;
            }
            // 这个field就是上面起的别名
            ParsedCardinality parsedCardinality = searchResponse.getAggregations().get(distinctField);
            size = (int) parsedCardinality.getValue();
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        return size;
    }

    /**
     * 分组统计匹配的数量（子聚合已去重）
     * @param indexName
     * @param field
     * @param paramsArray
     * @param distinctField
     * @param integralParams
     * @return
     */
    private Map<String, Long> countDistinctGroupBy(String indexName, String field, String[] paramsArray, String distinctField, IntegralParams integralParams) {
        SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, integralParams);

        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();
        boolQueryBuilder.must(QueryBuilders.termsQuery(field, paramsArray));

        // 创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(indexName);

        // 创建查询资源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        // 添加聚合分组条件，以字段 "your_field" 为例
        String aggregationTermsName = "group_by_authority";
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(aggregationTermsName).field(IntegralFields.AUTHORITY);

        // 统计去重
        CardinalityAggregationBuilder cardinalityAggregationBuilder = AggregationBuilders.cardinality(distinctField).field(distinctField);
        aggregation.subAggregation(cardinalityAggregationBuilder);

        sourceBuilder.aggregation(aggregation);
        sourceBuilder.size(0);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse;
        Map<String, Long> resultMap = new HashMap<>();
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null == searchResponse) {
                return null;
            }

            //获取聚合结果
            Aggregations aggregations = searchResponse.getAggregations();
            Terms groupAggregation = aggregations.get(aggregationTermsName);

            /**
             * 从分组中再分别查询对应法律的条目内容
             */
            for (Terms.Bucket bucket : groupAggregation.getBuckets()) {
                // 分组的键
                String key = bucket.getKeyAsString();
                // 分组中的文档数量
                long docCount = bucket.getDocCount();
                // 处理分组结果
                logger.debug("{}, {}", key, docCount);
                resultMap.put(key, docCount);
            }
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        return resultMap;
    }


    /**
     * 对每个效力级别进行分开统计总匹配数
     * @param indexName
     * @param integralParams
     * @return
     */
    public List<StatisticsRecord> countGroupByLawLevel(String indexName, IntegralParams integralParams) {
        IntegralParams copy = BeanUtil.toBean(integralParams, IntegralParams.class);

        String[] lawLevelArray = copy.getLawLevelArray();
        if(lawLevelArray == null || lawLevelArray.length == 0) {
            List<String> lawLevelOptions = lawCache.getLawLevelOptions();
            lawLevelArray = lawLevelOptions.toArray(new String[0]);
        }

        List<StatisticsRecord> resultList = new ArrayList<>();
        for(String lawLevel : lawLevelArray) {
            copy.setLawLevel(lawLevel);
            SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, copy);

            //long totalCount = this.countInEs(indexName, searchSourceBuilder);
            long totalCount = this.countGroupDistinct(indexName, searchSourceBuilder, IntegralFields.LAW_ID);

            if(totalCount > 0) {
                StatisticsRecord statisticsRecord = new StatisticsRecord();
                statisticsRecord.setName(lawLevel);
                statisticsRecord.setTotal(totalCount);
                resultList.add(statisticsRecord);
            }
        }
        return resultList;
    }


    /**
     * 对每个效力级别进行分开统计总匹配数
     *
     * 20240327 查看小包公，当查询"北京"和"北京市人大(含常委会)"时，右侧统计树，会显示两个北京，其中一个北京节点下有"北京市人大(含常委会)"
     * @param indexName
     * @param integralParams
     * @return
     */
    public List<TreeNode> countGroupByAuthority(String indexName, IntegralParams integralParams) {
        IntegralParams copy = BeanUtil.toBean(integralParams, IntegralParams.class);

        List<TreeNode> authorityTree = lawCache.getAuthorityTree();

        /**
         * 因为是List，filterAuthorityTree的元素就是各个查询维度的根节点
         */
        List<TreeNode> filterAuthorityTree = new ArrayList<>();
        if(ArrayUtil.isNotEmpty(copy.getAuthorityArray())) {
            /** 如果是具体的机构，就查这个机构归属的省市级别 */
            for(String label : copy.getAuthorityArray()) {
                for(TreeNode node : authorityTree) {
                    TreeNode targetNode = this.findNode(node, label);
                    if(targetNode != null) {
                        TreeNode parentNode = targetNode.getParent();
                        if(parentNode == null) {
                            continue;
                        }

                        /** 当目标是"北京市人大(含常委会)"时，不是直接返回"北京市人大(含常委会)"这个节点，
                         *  而是要把其父节点"北京"， 也返回
                         */
                        TreeNode copyParentNode = new TreeNode();
                        copyParentNode.setNodeType(parentNode.getNodeType());
                        copyParentNode.setLabel(parentNode.getLabel());

                        List<TreeNode> children = new ArrayList<>();
                        children.add(targetNode);
                        copyParentNode.setChildren(children);

                        filterAuthorityTree.add(copyParentNode);
                    }
                }
            }
        }

        if(ArrayUtil.isNotEmpty(copy.getAuthorityCityArray())) {
            /** 如果参数中是城市参数，就让filterAuthorityTree 有这些城市节点 */
            for(String label : copy.getAuthorityCityArray()) {
                for(TreeNode node : authorityTree) {
                    TreeNode targetNode = this.findNode(node, label);
                    if(targetNode != null) {
                        filterAuthorityTree.add(targetNode);
                    }
                }
            }
        }

        if(ArrayUtil.isNotEmpty(copy.getAuthorityProvinceArray())) {
            /** 如果参数中是省份参数，就让filterAuthorityTree 有这些省份节点 */
            for(String label : copy.getAuthorityProvinceArray()) {
                for(TreeNode node : authorityTree) {
                    TreeNode targetNode = this.findNode(node, label);
                    if(targetNode != null) {
                        filterAuthorityTree.add(targetNode);
                    }
                }
            }
        }

        if(filterAuthorityTree == null || filterAuthorityTree.isEmpty()) {
            /** filterAuthorityTree 经过筛选逻辑后如果没有值，那么就在页面上显示全树 */
            filterAuthorityTree = authorityTree;
        }

        filterAuthorityTree.forEach((item) -> this.dfsIterativeToStat(item, indexName, integralParams));

        /** 修剪树 */
        Iterator<TreeNode> iterator = filterAuthorityTree.iterator();
        while (iterator.hasNext()) {
            TreeNode element = iterator.next();
            if((long)element.getExtra() == 0) {
                /** 如果该节点的统计值为0，就直接删除 */
                iterator.remove();
                continue;
            }
            /** 修剪内部树结构 */
            this.dfsIterativeToTrimTree(element);
        }

        return filterAuthorityTree;
    }

    /**
     * 查找节点
     * @param root
     * @param label
     * @return
     */
    private TreeNode findNode(TreeNode root, String label) {
        if (root == null || StrUtil.isBlank(label)) {
            return null;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        /** 记录统计结果，避免重复查询 */
        while (!stack.isEmpty()) {
            TreeNode current = stack.pop();

            if(label.equals(current.getLabel())) {
                return current;
            }

            if(current.getChildren() != null) {
                // 将子节点压入栈中，以便后续遍历
                for (int i = current.getChildren().size() - 1; i >= 0; i--) {
                    stack.push(current.getChildren().get(i));
                }
            }
        }

        return null;
    }

    /**
     *
     * @param root
     * @param indexName
     * @param integralParams
     */
    private void dfsIterativeToStat(TreeNode root, String indexName, IntegralParams integralParams) {
        if (root == null) {
            return;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        /** 记录统计结果，避免重复查询 */
        Map<String, Long> authorityCountMap = new HashMap<>();
        while (!stack.isEmpty()) {
            TreeNode current = stack.pop();

            /**
             * authority/tree.json 已经构建了 org 与 province 的关系，所以不用关心 province 这块的数据
             */
            if("org".equals(current.getNodeType())) {
                /** org下可能是province，也可能是authority */
                List<TreeNode> nodeChildren = current.getChildren();
                if(nodeChildren == null) {
                    continue;
                }

                List<String> paramsList = new ArrayList<>();
                String childNodeType = null;
                for(TreeNode node : nodeChildren) {
                    if(childNodeType == null) {
                        childNodeType = node.getNodeType();
                    }
                    else {
                        if(!childNodeType.equals(node.getNodeType())) {
                            throw new IllegalStateException("Org TreeNode's children contains different node type");
                        }
                    }

                    paramsList.add(node.getLabel());
                }

                String field;
                String[] paramsArray = paramsList.toArray(new String[0]);
                if("province".equals(childNodeType)) {
                    /** 先获取之前的参数，然后再把省市信息的查询条件并入 */
                    field = IntegralFields.AUTHORITY_PROVINCE;
                }
                else if("authority".equals(childNodeType)) {
                    field = IntegralFields.AUTHORITY;
                }
                else if("org".equals(childNodeType)) {
                    field = IntegralFields.AUTHORITY;
                    //TODO 如果是org，则找其下所以的authority, 但是如果不点击org类型的节点就没事
                }
                else {
                    throw new IllegalStateException("Org TreeNode's node type is illegal");
                }

                /** 先获取之前的参数，然后再把省市信息的查询条件并入 */
                long totalCount = this.countGroupDistinct(indexName, field, paramsArray, IntegralFields.LAW_ID, integralParams);
                current.setExtra(totalCount);
            }
            else if("province".equals(current.getNodeType())) {
                /** 当前树节点是省级，就查市级 */
                List<TreeNode> cityChildren = current.getChildren();
                if(cityChildren == null) {
                    continue;
                }
                String[] cityArray = cityChildren.stream().map(TreeNode::getLabel).toArray(String[]::new);

                /** 先获取之前的参数，然后再把省市信息的查询条件并入 */
                long totalCount = this.countGroupDistinct(indexName, IntegralFields.AUTHORITY_CITY, cityArray, IntegralFields.LAW_ID, integralParams);
                current.setExtra(totalCount);
            }
            else if("city".equals(current.getNodeType())) {
                /** 当前树节点是市级，就查发布机关级 */
                List<TreeNode> authorityChildren = current.getChildren();
                if(authorityChildren == null) {
                    continue;
                }

                String[] authorityArray = authorityChildren.stream().map(TreeNode::getLabel).toArray(String[]::new);

                /** 先获取之前的参数，然后再把省市信息的查询条件并入 */
                Map<String, Long> map = this.countDistinctGroupBy(indexName, IntegralFields.AUTHORITY, authorityArray, IntegralFields.LAW_ID, integralParams);
                if(map != null) {
                    authorityCountMap.putAll(map);
                }

                AtomicLong total = new AtomicLong();
                map.forEach((k, v) -> total.addAndGet(v));
                current.setExtra(total.longValue());
            }
            else if("authority".equals(current.getNodeType())) {
                /** 当前树节点是机关级，就查之前市级分组查到的结果缓存中的值 */
                long total = authorityCountMap.getOrDefault(current.getLabel(), 0L);
                if(total == 0) {
                    /** 为0 的话就去真实查询一下 */
                    total = this.countGroupDistinct(indexName, IntegralFields.AUTHORITY,
                            new String[]{current.getLabel()}, IntegralFields.LAW_ID, integralParams);
                }
                current.setExtra(total);
            }

            if(current.getChildren() != null) {
                // 将子节点压入栈中，以便后续遍历
                for (int i = current.getChildren().size() - 1; i >= 0; i--) {
                    stack.push(current.getChildren().get(i));
                }
            }
        }
    }

    /**
     * 修剪树，把统计值为0 的节点都删去
     * @param root
     */
    private void dfsIterativeToTrimTree(TreeNode root) {
        /** 能进入方法的，内部统计值不能为0 */
        if (root == null) {
            return;
        }

        if((long)root.getExtra() == 0) {
            throw new IllegalArgumentException("Root treeNode stat count must be more than 0");
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode current = stack.pop();

            if(current.getChildren() != null) {
                // 将子节点压入栈中，以便后续遍历
                int childrenSize = current.getChildren().size();
                for (int i = childrenSize - 1; i >= 0; i--) {
                    TreeNode child = current.getChildren().get(i);
                    Long childStatCount = (Long) child.getExtra();
                    if(childStatCount == null || childStatCount == 0) {
                        /** 因为是倒着遍历，所以可以直接删除 */
                        current.getChildren().remove(i);
                    }
                    else {
                        stack.push(current.getChildren().get(i));
                    }
                }
            }
        }
    }

    /**
     * 构造查询条件
     * @param indexName
     * @param condition
     * @return
     */
    public SearchSourceBuilder mustConditions(String indexName, EsFields condition) {
        AbstractEsSrv esSrv = srvMap.get(indexName);
        if(esSrv == null) {
            throw new IllegalStateException("No es srv found");
        }

        return esSrv.mustConditions(condition);
    }


    /**
     * 从查询出来的法律中每个条目对应的历史
     * @param hitsList
     * @return
     */
    public Map<String, ProvisionHistory> makeLawProvisionHistory(List<IntegralFields> hitsList) {
        /** 使用set的原因是可能同一个法律可能命中多次，不能使用同样参数反复查询 */
        Set<LawProvision> lawProvisionList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            LawProvision lawProvision = new LawProvision();
            lawProvision.setLawName(fields.getLawNameOrigin());
            lawProvision.setTermTitle(fields.getTitleOrigin());
            lawProvisionList.add(lawProvision);
        }

        Map<String, ProvisionHistory> map = new HashMap<>();
        for(LawProvision lawProvision : lawProvisionList) {
            List<IntegralFields> matchHistoryList = this.listLawProvisionsHistory(lawProvision.getLawName(),
                    lawProvision.getTermTitle(),1000, new String[]{
                            IntegralFields.LAW_ID,
                            IntegralFields.LAW_NAME,
                            IntegralFields.PUBLISH,
                            IntegralFields.VALID_FROM,
                            IntegralFields.STATUS,
                            IntegralFields.TITLE,
                            IntegralFields.TERM_TEXT
                    });

            if(!matchHistoryList.isEmpty()) {
                ProvisionHistory provisionHistory = map.get(lawProvision.getLawName());
                if(provisionHistory == null) {
                    provisionHistory = new ProvisionHistory();
                }
                provisionHistory.addTermTitleHistory(lawProvision.getTermTitle(), matchHistoryList);
                map.put(lawProvision.getLawName(), provisionHistory);
            }
        }

        return map;
    }


    /**
     * 从查询出来的法律中每个条目对应的历史, 批量操作
     * 特别注意：
     * 如果不带法律状态条件，将可能查出多条同名法律，以至于这个历史返回的值是一样的
     *
     * @param hitsList
     * @return
     */
    public List<LawWithProvisionsMatched> makeLawWithProvisionsMatchedByBat(List<IntegralFields> hitsList, BoolQueryBuilder searchQueryForHistory) {
        /** 使用set的原因是可能同一个法律可能命中多次，不能使用同样参数反复查询 */
        Map<String, IntegralFields> itemOfHighlightMap = new HashMap<>(hitsList.size());
        Set<LawProvision> lawProvisionList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            LawProvision lawProvision = new LawProvision();
            lawProvision.setLawName(fields.getLawNameOrigin());
            lawProvision.setTermTitle(fields.getTitleOrigin());
            lawProvisionList.add(lawProvision);

            // 为了查历史的时候，让经过高亮定位的数据显示到前端
            itemOfHighlightMap.put(fields.getEsDocId(), fields);
        }

        Map<Long, LawWithProvisionsMatched> map = new HashMap<>();

        /**
         * 这些匹配值需要根据 lawProvision.getLawName() 进行分类
         */
        List<IntegralFields> matchHistoryList = this.listLawProvisionsHistoryByBat(lawProvisionList ,1000, null, searchQueryForHistory);

        if(!matchHistoryList.isEmpty()) {
            for(IntegralFields lawProvision : matchHistoryList) {
                IntegralFields itemOfHighlight = itemOfHighlightMap.get(lawProvision.getEsDocId());
                if(itemOfHighlight != null) {
                    BeanUtil.copyProperties(itemOfHighlight, lawProvision);
                }

                /** 开始分类 */
                LawWithProvisionsMatched lawWithProvisionsMatched = map.get(lawProvision.getLawId());
                if(lawWithProvisionsMatched == null) {
                    /** 如果map中没有对应值，就初始化一个 */
                    lawWithProvisionsMatched = new LawWithProvisionsMatched();
                    map.put(lawProvision.getLawId(), lawWithProvisionsMatched);
                }

                if(lawWithProvisionsMatched.getId() == null) {
                    lawWithProvisionsMatched.setId(lawProvision.getLawId());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getLevel())) {
                    lawWithProvisionsMatched.setLevel(lawProvision.getLawLevel());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getDocumentNo())) {
                    lawWithProvisionsMatched.setDocumentNo(lawProvision.getDocumentNo());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getLawName())) {
                    lawWithProvisionsMatched.setLawName(lawProvision.getLawName());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getLawNameOrigin())) {
                    lawWithProvisionsMatched.setLawNameOrigin(lawProvision.getLawNameOrigin());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getAuthority())) {
                    lawWithProvisionsMatched.setAuthority(lawProvision.getAuthority());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getAuthorityCity())) {
                    lawWithProvisionsMatched.setAuthorityCity(lawProvision.getAuthorityCity());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getAuthorityProvince())) {
                    lawWithProvisionsMatched.setAuthorityProvince(lawProvision.getAuthorityProvince());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getAuthorityDistrict())) {
                    lawWithProvisionsMatched.setAuthorityDistrict(lawProvision.getAuthorityDistrict());
                }

                if(StrUtil.isBlank(lawWithProvisionsMatched.getStatusLabel())) {
                    lawWithProvisionsMatched.setStatusLabel(lawProvision.getStatusLabel());
                }

                if(lawWithProvisionsMatched.getStatus() == null) {
                    lawWithProvisionsMatched.setStatus(lawProvision.getStatus());
                }

                if(lawWithProvisionsMatched.getPublish() == null) {
                    lawWithProvisionsMatched.setPublish(lawProvision.getPublish());
                }

                if(lawWithProvisionsMatched.getValidFrom() == null) {
                    lawWithProvisionsMatched.setValidFrom(lawProvision.getValidFrom());
                }

                lawProvision.setLawId(null);
                lawProvision.setLawLevel(null);
                lawProvision.setDocumentNo(null);
                lawProvision.setLawNameOrigin(null);
                lawProvision.setLawName(null);
                lawProvision.setAuthority(null);
                lawProvision.setAuthorityCity(null);
                lawProvision.setAuthorityProvince(null);
                lawProvision.setAuthorityDistrict(null);
                lawProvision.setStatusLabel(null);
                lawProvision.setStatus(null);
                lawProvision.setPublish(null);
                lawProvision.setValidFrom(null);
                lawWithProvisionsMatched.addProvision(lawProvision);
            }
        }

        List<LawWithProvisionsMatched> listSorted = new ArrayList<>();
        map.forEach((lawId, lawWithProvisionsMatched) -> {
            lawWithProvisionsMatched.getProvisionList().sort(Comparator.comparing(IntegralFields::getTitleNumber));
            listSorted.add(lawWithProvisionsMatched);
        });
        //listSorted.sort(Comparator.comparing(LawWithProvisionsMatched::getPublish));
        listSorted.sort((o1, o2) -> {
            if(o1.getPublish() == null) {
                return -1;
            }

            if(o2.getPublish() == null) {
                return -1;
            }

            return o2.getPublish().compareTo(o1.getPublish());
        });

        return listSorted;
    }

    /**
     * 关联文件也一并查出来
     * @param matchLawHits
     * @return
     */
    private Map<Long, List<IntegralFields>> makeAssociatedFiles(LawSearchHits matchLawHits) {
        List<IntegralFields> hitsList = matchLawHits.getSearchHits();
        return  this.makeAssociatedFiles(hitsList);
    }

    /**
     * 关联文件也一并查出来
     * @param hitsList
     * @return
     */
    public Map<Long, List<IntegralFields>> makeAssociatedFiles(List<IntegralFields> hitsList) {
        Set<Long> lawIdList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawIdList.add(fields.getLawId());
        }

        Map<Long, List<IntegralFields>> map = new HashMap<>(lawIdList.size());
        for(Long lawId : lawIdList) {
            IntegralParams param = new IntegralParams();
            param.setLawId(lawId);
            SearchSourceBuilder searchSourceBuilderOfProvision = this.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, param);
            LawSearchHits matchAssociatedFileHits = this.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                    1, 1000, null, null, null, null, searchSourceBuilderOfProvision);

            if(!matchAssociatedFileHits.getSearchHits().isEmpty()) {
                map.put(lawId, matchAssociatedFileHits.getSearchHits());
            }
        }
        return map;
    }
}
