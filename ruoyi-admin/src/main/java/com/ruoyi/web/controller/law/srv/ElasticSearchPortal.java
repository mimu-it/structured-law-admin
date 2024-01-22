package com.ruoyi.web.controller.law.srv;

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
import com.ruoyi.web.controller.law.api.domain.inner.AuthorityTreeNode;
import com.ruoyi.web.controller.law.api.domain.inner.StatisticsRecord;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHits;
import com.ruoyi.web.controller.law.api.domain.resp.SuggestHits;
import com.ruoyi.web.controller.law.cache.LawCache;
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
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

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
    AbstractEsSrv esLawAssociatedFileSrv;

    @Resource
    LawCache lawCache;

    Map<String, AbstractEsSrv> srvMap = new HashMap<>();

    @PostConstruct
    public void initSrvMapping() {
        srvMap.put(INDEX__LAW, esLawSrv);
        srvMap.put(INDEX__LAW_PROVISION, esLawProvisionSrv);
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
    public List<SuggestHits> suggest(String indexName, String suggestField, String suggestValue, String[] includeFields, String[] excludeFields) {
        String suggestionName = suggestField + "_suggest";

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        // 构建completionSuggestionBuilder传入查询的参数
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion(suggestField)
                .skipDuplicates(true).prefix(suggestValue).size(10);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        // 定义查询的suggest名称
        suggestBuilder.addSuggestion(suggestionName, completionSuggestionBuilder);
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

        int pageNum = 0;
        int pageSize = isProd? 1000 : 50;
        Page<IntegralFields> page = new Page<>();
        /**
         * 分页往 elasticsearch 中插入数据
         */
        while (pageNum == 0 || page.size() > 0) {
            if(pageNum != 0 && page.isEmpty()) {
                /** 如果不是第0页，并且最近一次查询得到的数据是空的，说明遍历到尾了，应该退出 */
                break;
            }

            if(!page.getResult().isEmpty()) {
                /** 如果有数据，就批量插入 elasticsearch 中 */
                try {
                    List<IntegralFields> rowList = page.getResult();
                    this.bulkInsert(indexName, rowList);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            /** 查询当前页的数据 */
            pageNum++;

            AbstractEsSrv esSrv = srvMap.get(indexName);
            if(esSrv == null) {
                throw new IllegalStateException("No es srv found");
            }

            page = esSrv.listDataByPage(pageNum, pageSize);
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
                new String[]{ IntegralFields.TERM_TEXT, IntegralFields.LAW_NAME },
                searchRequest, searchSourceBuilder);
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
        boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.LAW_NAME, lawName));
        if(StrUtil.isNotBlank(title)) {
            boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(IntegralFields.TITLE, title));
        }

        searchSourceBuilder.query(boolQueryBuilder);

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
    public LawSearchHits searchByPage(String indexName, int pageNum, int pageSize, String[] fields, String[] highlightFields,
                                      String sortField, Boolean sortType, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest request = new SearchRequest(indexName);
        if (ArrayUtil.isNotEmpty(fields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            searchSourceBuilder.fetchSource(new FetchSourceContext(true, fields, Strings.EMPTY_ARRAY));
        }

        /** from 是从 0 开始的， 设置确定结果要从哪个索引开始搜索的from选项，默认为0 */
        int from = pageNum - 1;
        from = from <= 0 ? 0 : from * pageSize;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);

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

        //logger.info("==" + response.getHits().getTotalHits());
        if (response.status().getStatus() == 200) {
            // 解析对象
            LawSearchHits highlightSearchHits = remakeSearchResponse(response, highlightFields);
            highlightSearchHits.setPageNum(pageNum);
            highlightSearchHits.setPageSize(pageSize);
            return highlightSearchHits;
        }
        return null;
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
        LawSearchHits highlightSearchHits = new LawSearchHits();

        long totalHitsCount = searchResponse.getHits().getTotalHits().value;

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        //解析结果
        ArrayList<IntegralFields> list = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            mixHighlight(highlightFields, list, hit);
        }

        highlightSearchHits.setTotal(totalHitsCount);
        highlightSearchHits.setSearchHits(list);
        return highlightSearchHits;
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
     * @param integralFields
     * @return
     */
    public List<StatisticsRecord> countGroupByStatus(String indexName, IntegralFields integralFields) {
        integralFields.setStatus(null);

        List<StatisticsRecord> resultList = new ArrayList<>();
        Map<Integer, String> statusTypesMap = lawCache.getStatusOptionsMap();
        statusTypesMap.forEach((k, v) -> {
            integralFields.setStatus(k);
            SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, integralFields);

            //long totalCount = this.countInEs(indexName, searchSourceBuilder);
            long totalCount = this.countGroupDistinct(indexName, searchSourceBuilder, IntegralFields.LAW_ID);

            StatisticsRecord statisticsRecord = new StatisticsRecord();
            statisticsRecord.setName(String.valueOf(k));
            statisticsRecord.setTotal(totalCount);
            resultList.add(statisticsRecord);
        });

        integralFields.setStatus(null);
        return resultList;
    }


    /**
     * 查询的数据可用 distinct 去重
     * @param indexName
     * @param searchSourceBuilder
     * @param field
     * @return
     */
    private Integer countGroupDistinct(String indexName, SearchSourceBuilder searchSourceBuilder, String field) {
        // 创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 创建查询资源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();
        sourceBuilder.query(boolQueryBuilder);

        // 查询条件-分组
        CardinalityAggregationBuilder cardinalityAggregationBuilder = AggregationBuilders.cardinality(field).field(field);
        sourceBuilder.aggregation(cardinalityAggregationBuilder);
        sourceBuilder.size(0);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse;
        int size = 0;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null == searchResponse) {
                return 0;
            }
            // 这个field就是上面起的别名
            ParsedCardinality parsedCardinality = searchResponse.getAggregations().get(field);
            size = (int) parsedCardinality.getValue();
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
        return size;
    }


    /**
     * 对每个效力级别进行分开统计总匹配数
     * @param indexName
     * @param integralFields
     * @return
     */
    public List<StatisticsRecord> countGroupByLawLevel(String indexName, IntegralFields integralFields) {
        integralFields.setLawLevel(null);

        List<StatisticsRecord> resultList = new ArrayList<>();
        List<String> lawLevelOptions = lawCache.getLawLevelOptions();
        lawLevelOptions.forEach((item) -> {
            integralFields.setLawLevel(item);
            SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, integralFields);

            //long totalCount = this.countInEs(indexName, searchSourceBuilder);
            long totalCount = this.countGroupDistinct(indexName, searchSourceBuilder, IntegralFields.LAW_ID);

            StatisticsRecord statisticsRecord = new StatisticsRecord();
            statisticsRecord.setName(item);
            statisticsRecord.setTotal(totalCount);
            resultList.add(statisticsRecord);
        });

        integralFields.setLawLevel(null);
        return resultList;
    }


    /**
     * 对每个效力级别进行分开统计总匹配数
     * @param indexName
     * @param integralFields
     * @return
     */
    public List<StatisticsRecord> countGroupByAuthorityProvince(String indexName, IntegralFields integralFields) {
        integralFields.setAuthority(null);
        integralFields.setAuthorityProvince(null);
        integralFields.setAuthorityCity(null);
        integralFields.setAuthorityDistrict(null);

        List<StatisticsRecord> resultList = new ArrayList<>();
        List<AuthorityTreeNode> authorityTree = lawCache.getAuthorityTree();

        Map<String, String[]> provinceValueMap = new HashMap<>();
        List<String> treeFlat = new ArrayList<>();
        authorityTree.forEach((item) -> {
            String labelName = item.getLabel();
            if("全国人大及其常委会".equals(labelName)) {
                List<String> authorityProvinceList = new ArrayList<>();
                for(AuthorityTreeNode province : item.getChildren()) {
                    authorityProvinceList.add(province.getLabel());
                }

                String[] provinceArray = authorityProvinceList.toArray(new String[0]);
                String provinceForShow = "全国性机关";
                treeFlat.add(provinceForShow);
                provinceValueMap.put(provinceForShow, provinceArray);
            }
            else {
                List<AuthorityTreeNode> provinceList = item.getChildren();
                provinceList.forEach((ch) -> {
                    String provinceName = ch.getLabel();
                    treeFlat.add(provinceName);
                });
            }
        });

        treeFlat.forEach((labelName) -> {
            String[] provinceArray = provinceValueMap.get(labelName);

            SearchSourceBuilder searchSourceBuilder = this.mustConditions(indexName, integralFields);


            BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();

            if(provinceArray != null) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(IntegralFields.AUTHORITY_PROVINCE, provinceArray));
            }
            else {
                boolQueryBuilder.must(QueryBuilders.termQuery(IntegralFields.AUTHORITY_PROVINCE, labelName));
            }

            searchSourceBuilder.query(boolQueryBuilder);

            //long totalCount = this.countInEs(indexName, searchSourceBuilder);
            long totalCount = this.countGroupDistinct(indexName, searchSourceBuilder, IntegralFields.LAW_ID);

            StatisticsRecord statisticsRecord = new StatisticsRecord();
            statisticsRecord.setName(labelName);
            statisticsRecord.setTotal(totalCount);
            resultList.add(statisticsRecord);
        });

        integralFields.setAuthority(null);
        integralFields.setAuthorityProvince(null);
        integralFields.setAuthorityCity(null);
        integralFields.setAuthorityDistrict(null);
        return resultList;
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

}
