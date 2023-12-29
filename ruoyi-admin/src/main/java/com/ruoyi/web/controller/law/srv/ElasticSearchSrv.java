package com.ruoyi.web.controller.law.srv;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralProvision;
import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 */
@Service
public class ElasticSearchSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INDEX__LAW = "law";

    @Autowired
    ElasticsearchClient client;

    /**
     * 定义索引
     */
    public void initIndex() {
        try {
            BooleanResponse existsResponse = client.indices().exists(b -> b.index(INDEX__LAW));
            if(existsResponse.value()) {
                logger.info("索引已存在");
                return;
            }

            File file = ResourceUtils.getFile("classpath:elasticsearch/index_law_mappings.json");
            String mappings = FileUtil.readString(file, Charset.defaultCharset());
            JsonpMapper mapper = client._transport().jsonpMapper();
            JsonParser parser = Json.createParser(new StringReader(mappings));

            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(INDEX__LAW)
                    .mappings(TypeMapping._DESERIALIZER.deserialize(parser, mapper)).build();

            client.indices().create(createIndexRequest);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }


    /**
     * 每个方法执行前都会执行
     * @throws IOException
     */
    public boolean deleteIndexOfLaw() throws IOException {
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(f -> f.index(INDEX__LAW));
        return deleteIndexResponse.acknowledged();
    }


    /**
     * 新建索引
     */
    public boolean insertLawToIndex(IntegralProvision integralProvision) {
        try {
            IndexResponse indexResponse = client.index(i -> {
                IndexRequest.Builder<Object> builder = i.index(INDEX__LAW).document(integralProvision);
                if(integralProvision.getProvisionId() != null) {
                    builder.id(String.valueOf(integralProvision.getProvisionId()));
                }
                return builder;
            });

            return true;
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }


    /**
     * 批量新增
     * @throws IOException
     */
    public void bulkInsert(List<IntegralProvision> list)  {
        if(list.isEmpty()) {
            return;
        }
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (IntegralProvision item : list) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX__LAW)
                            .id(String.valueOf(item.getProvisionId()))
                            .document(item)
                    )
            );
        }

        try {
            BulkResponse result = client.bulk(br.build());

            if (result.errors()) {
                for (BulkResponseItem item: result.items()) {
                    if (item.error() != null) {
                        logger.error(item.error().reason());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }


    /**
     * 通过id查询
     * @throws IOException
     */
    public IntegralProvision getByProvisionId(long provisionId) throws IOException {
        GetResponse<IntegralProvision> response = client.get(g -> g
                        .index(INDEX__LAW)
                        .id(String.valueOf(provisionId)),
                IntegralProvision.class
        );
        return response.source();
    }

    /**
     * 不同的页码，得到不同的命中数据
     * @param pageNum
     * @return
     * @throws IOException
     */
    public SearchResponse<IntegralProvision> searchByPage(int pageNum, IntegralProvision condition) {
        int pageSize = 10;
        try {
            SearchResponse<IntegralProvision> response = client.search(s -> s
                            .index(INDEX__LAW)
                            .query(q -> q.bool(b -> {
                                    return mustConditions(b, condition);
                                })
                            )
                            .highlight(h -> h
                                    .preTags("<span style='color: red'>")
                                    .postTags("</span>")
                                    .fields(StrUtil.toCamelCase(IntegralProvision.TERM_TEXT), highlightFieldBuilder -> highlightFieldBuilder)
                                    .fields(StrUtil.toCamelCase(IntegralProvision.LAW_NAME), highlightFieldBuilder -> highlightFieldBuilder)
                                    .fields(IntegralProvision.SUBTITLE, highlightFieldBuilder -> highlightFieldBuilder)
                                    .fields(IntegralProvision.TITLE, highlightFieldBuilder -> highlightFieldBuilder)
                            )
                            .from((pageNum - 1) * pageSize)
                            .size(pageSize),
                    IntegralProvision.class
            );
            return response;
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 构造 elasticsearch 查询条件
     * @param builder
     * @param condition
     * @return
     */
    private BoolQuery.Builder mustConditions(BoolQuery.Builder builder, IntegralProvision condition) {
        if(StrUtil.isNotBlank(condition.getLawName())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.LAW_NAME))
                    .query(FieldValue.of(condition.getLawName()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getFolder())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.FOLDER))
                    .query(FieldValue.of(condition.getFolder()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getLawType())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.LAW_TYPE))
                    .query(FieldValue.of(condition.getLawType()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getSubtitle())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.SUBTITLE))
                    .query(FieldValue.of(condition.getSubtitle()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getTags())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.TAGS))
                    .query(FieldValue.of(condition.getTags()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getTermText())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.TERM_TEXT))
                    .query(FieldValue.of(condition.getTermText()))
            )._toQuery();
            builder.must(query);
        }

        if(StrUtil.isNotBlank(condition.getTitle())) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.TITLE))
                    .query(FieldValue.of(condition.getTitle()))
            )._toQuery();
            builder.must(query);
        }

        if(condition.getPublish() != null) {
            String dateStr = DateUtil.format(condition.getPublish(), "yyyy-MM-dd");
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.PUBLISH))
                    .query(FieldValue.of(dateStr))
            )._toQuery();
            builder.must(query);
        }

        if(condition.getValidFrom() != null) {
            String dateStr = DateUtil.format(condition.getValidFrom(), "yyyy-MM-dd");
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.VALID_FROM))
                    .query(FieldValue.of(dateStr))
            )._toQuery();
            builder.must(query);
        }

        if(condition.getStatus() != null) {
            Query query = MatchQuery.of(m -> m
                    .field(StrUtil.toCamelCase(IntegralProvision.STATUS))
                    .query(FieldValue.of(condition.getStatus()))
            )._toQuery();
            builder.must(query);
        }

        return builder;
    }
}
