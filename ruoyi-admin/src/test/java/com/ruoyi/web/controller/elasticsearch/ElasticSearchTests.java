package com.ruoyi.web.controller.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.JsonData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 *
 * 使用 elasticsearch java api client
 */
@SpringBootTest
public class ElasticSearchTests {
    private static final String INDEX__BOOK = "book";
    private static final String EXISTS__ID = "321";
    private static final String EXISTS__NAME = "三体";

    @Autowired
    ElasticsearchClient client;


    /**
     * 每个方法执行前都会执行
     * @throws IOException
     */
    @BeforeEach
    public void deleteBook() throws IOException {
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(f -> f.index(INDEX__BOOK));
        System.out.println(deleteIndexResponse.acknowledged());
    }


    /**
     * 新建索引
     */
    @Test
    public void insert() throws IOException {
        Book book = new Book(EXISTS__NAME, new Date(), "三体是天体力学名词，指的是由三个质点在相互引力的作用下如何运动的问题");
        IndexResponse indexResponse = client.index(i -> i.index(INDEX__BOOK).id(EXISTS__ID).document(book));
        Assertions.assertTrue(indexResponse.id().equals(EXISTS__ID));
    }


    /**
     * 批量新增
     * @throws IOException
     */
    @Test
    public void bulkInsert() throws IOException {

        List<Book> books = new ArrayList<>();
        books.add(new Book("正见", new Date(), "真正的佛学不是现在这样"));
        books.add(new Book("Python AI", new Date(), "机器学习是未来的趋势"));
        books.add(new Book("民法典", new Date(), "农村集体经济组织依法取得法人资格。法律、行政法规对农村集体经济组织有规定的，依照其规定。"));
        books.add(new Book("公司法", new Date(), "公司股东依法享有资产收益、参与重大决策和选择管理人等权利。"));
        books.add(new Book("公司界定及股东责任", new Date(), "公司是企业法人，有独立的法人财产，享有法人财产权。"));

        BulkRequest.Builder br = new BulkRequest.Builder();

        int id = 1000;
        for (Book book : books) {
            String _id = String.valueOf(id++);
            br.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX__BOOK)
                            .id(_id)
                            .document(book)
                    )
            );
        }

        BulkResponse result = client.bulk(br.build());

        if (result.errors()) {
            System.out.println("Bulk had errors");
            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    System.out.println(item.error().reason());
                }
            }
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过id查询
     * @throws IOException
     */
    @Test
    public void readingByID() throws IOException {
        this.insert();

        GetResponse<Book> response = client.get(g -> g
                        .index(INDEX__BOOK)
                        .id(EXISTS__ID),
                Book.class
        );

        Assertions.assertTrue(response.found());
        Book book = response.source();
        System.out.println("book name " + book.getName());
        Assertions.assertEquals(EXISTS__NAME, book.getName());
    }

    /**
     * 查询条件查询
     * @throws IOException
     */
    @Test
    public void search() throws IOException {
        this.bulkInsert();

        String searchText = "法人";
        SearchResponse<Book> response = client.search(s -> s
                        .index(INDEX__BOOK)
                        .query(q -> q
                                .match(t -> t
                                        .field("content")
                                        .query(FieldValue.of(searchText))
                                )
                        ),
                Book.class
        );

        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            System.out.println("There are " + total.value() + " results");
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }

        List<Hit<Book>> hits = response.hits().hits();
        for (Hit<Book> hit: hits) {
            Book book = hit.source();
            System.out.println("Found Book " + book.getContent() + ", score " + hit.score());
        }
    }


    /**
     *  嵌套查询
     */
    @Test
    public void nestedSearch() throws IOException {
        this.bulkInsert();

        String searchText = "法人";
        Date now = new Date();

        // Search by content
        Query byContent = MatchQuery.of(m -> m
                .field("content")
                .query(FieldValue.of(searchText))
        )._toQuery();

        // Search by date
        Query byPublish = RangeQuery.of(r -> r
                .field("publish")
                .lte(JsonData.of(now))
        )._toQuery();

        // Combine name and price queries to search the product index
        SearchResponse<Book> response = client.search(s -> s
                        .index(INDEX__BOOK)
                        .query(q -> q
                                .bool(b -> b
                                        .must(byContent)
                                        .must(byPublish)
                                )
                        ),
                Book.class
        );
        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            System.out.println("There are " + total.value() + " results");
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }
        List<Hit<Book>> hits = response.hits().hits();
        for (Hit<Book> hit : hits) {
            Book book = hit.source();
            System.out.println(String.format("Found Book %s, content: %s, score: %s",
                    book.getName(), book.getContent(), hit.score()));
        }
    }

    /**
     * 分页查询 第1页有数据
     * @throws IOException
     */
    @Test
    public void getEsDataByPageHasHits() throws IOException {
        this.bulkInsert();

        List<Hit<Book>> hits = searchByPage(1);

        Assertions.assertTrue(hits.size() == 3);
        for (Hit<Book> hit : hits) {
            Book book = hit.source();
            System.out.println(String.format("Found Book %s, content: %s, score: %s",
                    book.getName(), book.getContent(), hit.score()));

            /**
             * highlightMap 是保存了带高亮标签的完整句子
             */
            Map<String, List<String>> highlightMap =  hit.highlight();
            System.out.println(highlightMap);
        }
    }

    /**
     * 分页查询 第2页无数据
     * @throws IOException
     */
    @Test
    public void getEsDataByPageNoneHits() throws IOException {
        this.bulkInsert();

        List<Hit<Book>> hits = searchByPage(2);

        Assertions.assertTrue(hits.size() == 0);
    }

    /**
     * 不同的页码，得到不同的命中数据
     * @param pageNum
     * @return
     * @throws IOException
     */
    private List<Hit<Book>> searchByPage(int pageNum) throws IOException {
        int pageSize = 10;
        String searchText = "法人";
        SearchResponse<Book> response = client.search(s -> s
                        .index(INDEX__BOOK)
                        .query(q -> q.match(t -> t
                                        .field("content")
                                        .query(FieldValue.of(searchText))
                                )
                        )
                        .highlight(h -> h
                                .preTags("<span style='color: red'>")
                                .postTags("</span>")
                                .fields("content", highlightFieldBuilder -> highlightFieldBuilder)
                        )
                        .from((pageNum - 1) * pageSize)
                        .size(pageSize),
                Book.class
        );
        TotalHits total = response.hits().total();
        System.out.println(String.format("%s results, %s hits", total.value(), response.hits().hits().size()));
        return response.hits().hits();
    }


    public static class Book {
        private String name;
        private Date publish;
        private String content;

        public Book() {
            // readingByID 必需
        }

        public Book(String name, Date publish, String content) {
            this.publish = publish;
            this.name = name;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getPublish() {
            return publish;
        }

        public void setPublish(Date publish) {
            this.publish = publish;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
