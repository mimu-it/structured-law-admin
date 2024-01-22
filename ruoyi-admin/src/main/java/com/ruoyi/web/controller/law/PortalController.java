package com.ruoyi.web.controller.law;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.inner.Statistics;
import com.ruoyi.web.controller.law.api.domain.inner.StatisticsRecord;
import com.ruoyi.web.controller.law.api.domain.resp.*;
import com.ruoyi.web.controller.law.cache.LawCache;
import com.ruoyi.web.controller.law.srv.ElasticSearchPortal;
import io.swagger.annotations.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 *
 * 查看接口文档 http://localhost:8080/swagger-ui/index.html#/portal-controller
 *
 */
@Api("法派法库服务接口")
@RestController
@RequestMapping("/structured-law/portal")
public class PortalController extends BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LOCK_ID_BUILD_ES_INDEX = "redis_lock_for_build_es_index";
    private static final String LOCK_ID_PARSE_MD = "redis_lock_for_parse_md";

    @Resource
    private LawCache lawCache;

    @Resource
    private ElasticSearchPortal elasticSearchPortal;

    @Resource
    private ISlLawService slLawService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${backup.path}")
    private String backupPath;

    @Value("${spring.datasource.druid.master.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.druid.master.username}")
    private String dbUsername;

    @Value("${spring.datasource.druid.master.password}")
    private String dbPassword;

    @Value("${law.structured.sync.api-host}")
    private String syncLawApiHost;

    @Value("${law.structured.sync.api-port}")
    private String syncLawApiPort;


    /**
     * 定义正则表达式模式
     */
    private static Pattern pattern = Pattern.compile("\\d+");


    @PostConstruct
    public void resetLock() {
        redisTemplate.delete(LOCK_ID_BUILD_ES_INDEX);
    }


    /**
     * 从查询出来的法律中对应找法律的历史
     * @param matchLawHits
     * @return
     */
    private Map<String, List<IntegralFields>> makeLawHistory(LawSearchHits matchLawHits) {
        List<IntegralFields> hitsList = matchLawHits.getSearchHits();
        return this.makeLawHistory(hitsList);
    }

    /**
     * 从查询出来的法律中对应找法律的历史
     * @param hitsList
     * @return
     */
    private Map<String, List<IntegralFields>> makeLawHistory(List<IntegralFields> hitsList) {
        Set<String> lawNameList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawNameList.add(fields.getLawNameOrigin());
        }

        Map<String, List<IntegralFields>> map = new HashMap<>(lawNameList.size());
        for(String name : lawNameList) {
            List<IntegralFields> matchHistoryList = elasticSearchPortal.listLawHistory(name, 1000, new String[]{
                    IntegralFields.LAW_NAME,
                    IntegralFields.PUBLISH,
                    IntegralFields.VALID_FROM,
                    IntegralFields.STATUS
            });

            if(!matchHistoryList.isEmpty()) {
                map.put(name, matchHistoryList);
            }
        }

        return map;
    }

    /**
     * 从查询出来的法律中对应找法律的历史
     * @param matchLawHits
     * @param title
     * @return
     */
    private Map<String, List<IntegralFields>> makeLawProvisionHistory(LawSearchHits matchLawHits, String title) {
        List<IntegralFields> hitsList = matchLawHits.getSearchHits();
        return this.makeLawProvisionHistory(hitsList, title);
    }

    /**
     * 从查询出来的法律中对应找法律的历史
     * @param hitsList
     * @return
     */
    private Map<String, List<IntegralFields>> makeLawProvisionHistory(List<IntegralFields> hitsList, String title) {
        Set<String> lawNameList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawNameList.add(fields.getLawNameOrigin());
        }

        Map<String, List<IntegralFields>> map = new HashMap<>(lawNameList.size());
        for(String name : lawNameList) {
            List<IntegralFields> matchHistoryList = elasticSearchPortal.listLawProvisionsHistory(name, title,1000, new String[]{
                    IntegralFields.LAW_NAME,
                    IntegralFields.PUBLISH,
                    IntegralFields.VALID_FROM,
                    IntegralFields.STATUS
            });

            if(!matchHistoryList.isEmpty()) {
                map.put(name, matchHistoryList);
            }
        }

        return map;
    }


    /**
     * http://localhost:8080/structured-law/portal/wild-search-associate?page_num=1&page_size=10&law_id=8associated_file_name=草案
     *
     * http://localhost:8080/structured-law/portal/wild-search-associate?page_num=1&page_size=10&associated_file_name=中华人民共和国对外关系法
     * http://localhost:8080/structured-law/portal/wild-search-associate?page_num=1&page_size=10&document_type=SYJGDBG
     *
     *
     * @param pageNum
     * @param pageSize
     * @param lawId
     * @return
     */
    @ApiOperation(value="关键词查询关联文件内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = IntegralFields.LAW_ID, value = "法律id", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.CONTENT_TEXT, value = "正文", dataType = "String", dataTypeClass = String.class),
    })
    @GetMapping("/wild-search-associate")
    public R<LawSearchHits> wildSearchAssociateFile(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                                    @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                                    @RequestParam(value = IntegralFields.LAW_ID, required = false) String lawId,
                                                    @RequestParam(value = IntegralFields.DOCUMENT_TYPE, required = false) String documentType,
                                                    @RequestParam(value = IntegralFields.ASSOCIATED_FILE_NAME, required = false) String associatedFileName,
                                                    @RequestParam(value = IntegralFields.CONTENT_TEXT, required = false) String contentText) {
        IntegralFields integralFields = new IntegralFields();
        if (StrUtil.isNotBlank(lawId)) {
            integralFields.setLawId(Long.parseLong(lawId));
        }

        if (StrUtil.isNotBlank(contentText)) {
            integralFields.setContentText(contentText);
        }

        if (StrUtil.isNotBlank(associatedFileName)) {
            integralFields.setAssociatedFileName(associatedFileName);
        }

        if (StrUtil.isNotBlank(documentType)) {
            integralFields.setDocumentType(documentType);
        }

        SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchPortal.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, integralFields);
        LawSearchHits matchAssociatedFileHits = elasticSearchPortal.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                pageNum, pageSize, null,
                new String[]{ IntegralFields.CONTENT_TEXT }, null, null, searchSourceBuilderOfProvision);

        return R.ok(matchAssociatedFileHits);
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
    private Map<Long, List<IntegralFields>> makeAssociatedFiles(List<IntegralFields> hitsList) {
        Set<Long> lawIdList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawIdList.add(fields.getLawId());
        }

        Map<Long, List<IntegralFields>> map = new HashMap<>(lawIdList.size());
        for(Long lawId : lawIdList) {
            IntegralFields param = new IntegralFields();
            param.setLawId(lawId);
            SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchPortal.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, param);
            LawSearchHits matchAssociatedFileHits = elasticSearchPortal.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                    1, 1000, null, null, null, null, searchSourceBuilderOfProvision);

            if(!matchAssociatedFileHits.getSearchHits().isEmpty()) {
                map.put(lawId, matchAssociatedFileHits.getSearchHits());
            }
        }
        return map;
    }

    /**
     * 公共属性的组装
     * @param documentNoMulti
     * @param publishRange
     * @param validFromRange
     * @param status
     * @param authority
     * @param authorityProvince
     * @param authorityCity
     * @param authorityDistrict
     * @param integralFields
     */
    private void makeParamForCommon(String documentNoMulti,
                                    String publishRange,
                                    String validFromRange,
                                    Integer status,
                                    String authority,
                                    String authorityProvince,
                                    String authorityCity,
                                    String authorityDistrict, IntegralFields integralFields) {
        if (StrUtil.isNotBlank(documentNoMulti)) {
            String[] documentNoMultiStrArray = JSONUtil.toList(documentNoMulti, String.class).toArray(new String[0]);
            integralFields.setDocumentNoArray(documentNoMultiStrArray);
        }

        if (StrUtil.isNotBlank(publishRange)) {
            integralFields.setPublishRange(publishRange);
        }

        if (StrUtil.isNotBlank(validFromRange)) {
            integralFields.setPublishRange(validFromRange);
        }

        if (status != null) {
            integralFields.setStatus(status);
        }

        if (StrUtil.isNotBlank(authority)) {
            integralFields.setAuthority(authority);
        }

        if (StrUtil.isNotBlank(authorityProvince)) {
            integralFields.setAuthorityProvince(authorityProvince);
        }

        if (StrUtil.isNotBlank(authorityCity)) {
            integralFields.setAuthorityCity(authorityCity);
        }

        if (StrUtil.isNotBlank(authorityDistrict)) {
            integralFields.setAuthorityDistrict(authorityDistrict);
        }
    }


    /**
     * 获取法律详情, 可获取全文结构树
     * [已测， 注意如果es内存不够会提示出错，奔溃] http://localhost:8080/structured-law/portal/law-content?law_id=110
     *
     * @param lawId
     * @param size
     * @return
     */
    @ApiOperation(value="法律id查询法律及法律正文内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = IntegralFields.LAW_ID, value = "法律id", dataType = "long", dataTypeClass = Long.class),
            @ApiImplicitParam(name = Constants.SIZE, value = "显示的条款数量(eg. 1000)", dataType = "int", dataTypeClass = Integer.class),
    })
    @GetMapping("/law-content")
    public R<LawDetail> showLawContent(@RequestParam(IntegralFields.LAW_ID) long lawId,
                                     @RequestParam(value = Constants.SIZE, required = false) Integer size) {
        /** 首先获取法律的基本信息 */
        List<IntegralFields> matchOne = elasticSearchPortal.listProvisionsByLawId(lawId, null,1, new String[]{
                IntegralFields.LAW_ID,
                IntegralFields.LAW_NAME,
                IntegralFields.LAW_LEVEL,
                IntegralFields.AUTHORITY,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM,
                IntegralFields.STATUS,
                IntegralFields.DOCUMENT_NO
        });

        if (matchOne == null || matchOne.isEmpty()) {
            return R.ok(null);
        }

        /** 首先获取法律的所有条款数据，其实已经自带全文结构树 */
        List<IntegralFields> matchList = elasticSearchPortal.listProvisionsByLawId(lawId, null, size, new String[]{
                IntegralFields.TITLE,
                IntegralFields.TERM_TEXT,
                IntegralFields.LAW_NAME,
                IntegralFields.LAW_ID,
                IntegralFields.PROVISION_ID,
                IntegralFields.AUTHORITY
        });


        /** 从查询结果中获取法律名称，并继续从es中获取历史 */
        Map<String, List<IntegralFields>> historyMap = this.makeLawHistory(matchList);

        /** 从查询结果中获取法律名称，并继续从es中获取关联文件 */
        Map<Long, List<IntegralFields>> associatedFileMap = this.makeAssociatedFiles(matchList);

        /** 为了获取法律的基本信息 */
        IntegralFields matchedItem = matchOne.get(0);

        /** 组装数据 */
        LawDetail lawDetail = new LawDetail();
        lawDetail.setId(matchedItem.getEsDocId());
        lawDetail.setAccording("");
        lawDetail.setAuthority(matchedItem.getAuthority());
        lawDetail.setProvisions(matchList);
        lawDetail.setDocumentNo(matchedItem.getDocumentNo());
        lawDetail.setLevel(matchedItem.getLawLevel());
        if (matchedItem.getPublish() != null) {
            lawDetail.setPublishAt(DateUtil.format(matchedItem.getPublish(), "yyyy-MM-dd"));
        }

        lawDetail.setStatus(String.valueOf(matchedItem.getStatus()));
        lawDetail.setLawName(matchedItem.getLawName());

        if (matchedItem.getValidFrom() != null) {
            lawDetail.setValidFrom(DateUtil.format(matchedItem.getValidFrom(), "yyyy-MM-dd"));
        }

        lawDetail.setHistoryMap(historyMap);
        lawDetail.setAssociatedFileMap(associatedFileMap);

        return R.ok(lawDetail);
    }

    /**
     * 法律条款id查询条款正文内容
     *
     * [已测]http://localhost:8080/structured-law/portal/law-provision-content?provision_id=110
     *
     * @param provisionId
     * @return
     */
    @ApiOperation(value="法律条款id查询条款正文内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = IntegralFields.PROVISION_ID, value = "法律条款id", dataType = "long", dataTypeClass = Long.class)
    })
    @GetMapping("/law-provision-content")
    public R<IntegralFields> showLawProvisionContent(@RequestParam(IntegralFields.PROVISION_ID) long provisionId) {
        /** 首先获取法律的基本信息 */
        IntegralFields matchOne = elasticSearchPortal.getByProvisionId(provisionId, new String[]{
                IntegralFields.LAW_ID,
                IntegralFields.LAW_NAME,
                IntegralFields.LAW_LEVEL,
                IntegralFields.AUTHORITY,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM,
                IntegralFields.STATUS,
                IntegralFields.DOCUMENT_NO,
                IntegralFields.TITLE,
                IntegralFields.TERM_TEXT,
                IntegralFields.PROVISION_ID
        });

        return R.ok(matchOne);
    }


    /**
     * 把所有的条款合并到一起组合成正文
     *
     * @param matchList
     * @return
     */
    private StringBuilder joinAllProvisions(List<IntegralFields> matchList) {
        StringBuilder sb = new StringBuilder();
        for (IntegralFields provision : matchList) {
            String title = provision.getTitle();
            int lastIdx = title.lastIndexOf("/");
            if (lastIdx != -1) {
                title = title.substring(lastIdx + 1);
            }
            sb.append(title).append(" ").append(provision.getTermText()).append("\n");
        }
        return sb;
    }


    /**
     * 检索某个法律的历史
     * [已测] http://localhost:8080/structured-law/portal/law-history?law_name=最高人民法院关于知识产权法庭若干问题的规定
     *
     * http://localhost:8080/structured-law/portal/law-history?law_id=123
     *
     * @param lawId
     * @param size
     * @return
     */
    @ApiOperation(value="查询某个法律的历史变更")
    @ApiImplicitParams({
            @ApiImplicitParam(name = IntegralFields.LAW_ID, value = "法律id", dataType = "long", dataTypeClass = Long.class),
            @ApiImplicitParam(name = Constants.SIZE, value = "显示的条款数量(eg. 1000)", dataType = "int", dataTypeClass = Integer.class),
    })
    @GetMapping("/law-history")
    public R<List<IntegralFields>> listLawHistory(@RequestParam(IntegralFields.LAW_ID) long lawId,
                                     @RequestParam(value = Constants.SIZE, required = false) Integer size) {
        SlLaw law = slLawService.getById(lawId, new String[]{
                SlLaw.NAME
        });

        if(law == null) {
            return R.ok(new ArrayList<>());
        }

        List<IntegralFields> matchList = elasticSearchPortal.listLawHistory(law.getName(), size, new String[]{
                IntegralFields.LAW_ID,
                IntegralFields.LAW_NAME,
                IntegralFields.STATUS,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM
        });
        return R.ok(matchList);
    }


    /**
     * 获取各个条件的选项
     * [已测]
     * @return
     */
    @ApiOperation(value="获取各查询条件的可选项")
    @GetMapping("/conditionsOptions")
    public R<LawSearchConditionOptions> conditionsOptions() {
        LawSearchConditionOptions options = new LawSearchConditionOptions();
        options.setLevelOptions(lawCache.getLawLevelOptions());
        options.setAuthorityOptions(lawCache.getAuthorityOptions());
        options.setStatusOptions(lawCache.getStatusOptions());
        options.setAuthorityTree(lawCache.getAuthorityTree());
        //collect_status 不知对应什么
        return R.ok(options);
    }

    /**
     * 如果未限定level，就分组查询
     * 限定了level,就按指定级别查询
     *
     *
     * [已测]http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&content_text=诈骗
     * [已测]http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&content_text=诈骗&law_level=["司法解释"]
     *
     * [已测]http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&title=第一章/第二条
     *
     * http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&law_name=最高人民法院关于适用《民法典》婚姻家庭编的解释（一）
     *
     * [已测]http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&authority=全国人民代表大会常务委员会
     *
     * 若依框架后端使用的响应对象AjaxResult，和Swagger存在不兼容问题，导致返回体即使使用了Swagger注解，但是Swagger接口文档中，不显示返回体的对象Swagger文档
     * @param pageNum
     * @param pageSize
     * @param lawLevelArrayStr
     * @param authority
     * @return
     */
    @ApiOperation(value="如果未限定level，就分组查询， 限定了level,就按指定级别查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.SORT_FIELD, value = "用于排序的字段", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = Constants.SORT_TYPE, value = "用于排序的类型，升序、降序", dataType = "Boolean", dataTypeClass = Boolean.class),
            @ApiImplicitParam(name = IntegralFields.CONTENT_TEXT, value = "搜索内容(既搜索法律名称，也搜索具体条款)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.LAW_NAME, value = "法律名称", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.TITLE, value = "法条小标题(eg. 第十二条)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.TERM_TEXT, value = "法条正文", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.DOCUMENT_NO, value = "发布文号(eg. 中华人民共和国主席令（第十三号）)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.PUBLISH, value = "发布日期(eg. 1995-03-28)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.VALID_FROM, value = "执行日期(eg. 1995-03-28)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.STATUS, value = "状态(eg. 1, 5, 9)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.LAW_LEVEL, value = "效力级别(eg. 宪法、法律、地方性法规...)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.AUTHORITY, value = "制定机关(eg. 最高人民法院、最高人民检察院...)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.AUTHORITY_PROVINCE, value = "制定机关所在省(eg. 湖南省)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.AUTHORITY_CITY, value = "制定机关所在市(eg. 长沙市)", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.AUTHORITY_DISTRICT, value = "制定机关所在区(eg. 开福区)", dataType = "String", dataTypeClass = String.class),
    })
    @GetMapping("/group/search-law")
    public R<LawSearchHitsGroup> searchGroupByLawLevel(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                                       @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                                       @RequestParam(value = Constants.SORT_FIELD, required = false) String sortField,
                                                       @RequestParam(value = Constants.SORT_TYPE, required = false) Boolean sortType,
                                                       @RequestParam(value = IntegralFields.CONTENT_TEXT, required = false) String contentText,
                                                       @RequestParam(value = IntegralFields.LAW_NAME, required = false) String lawName,
                                                       @RequestParam(value = IntegralFields.TERM_TEXT, required = false) String termText,
                                                       @RequestParam(value = IntegralFields.TITLE, required = false) String title,
                                                       @RequestParam(value = IntegralFields.DOCUMENT_NO, required = false) String documentNoMulti,
                                                       @RequestParam(value = IntegralFields.PUBLISH, required = false) String publishRange,
                                                       @RequestParam(value = IntegralFields.VALID_FROM, required = false) String validFromRange,
                                                       @RequestParam(value = IntegralFields.STATUS, required = false) Integer status,
                                                       @RequestParam(value = IntegralFields.AUTHORITY, required = false) String authority,
                                                       @RequestParam(value = IntegralFields.AUTHORITY_PROVINCE, required = false) String authorityProvince,
                                                       @RequestParam(value = IntegralFields.AUTHORITY_CITY, required = false) String authorityCity,
                                                       @RequestParam(value = IntegralFields.AUTHORITY_DISTRICT, required = false) String authorityDistrict,
                                                       @RequestParam(value = IntegralFields.LAW_LEVEL, required = false) String lawLevelArrayStr) {
        pageNum = pageNum < 1 ? 1 : pageNum;

        LawSearchHitsGroup lawSearchHitsGroup = new LawSearchHitsGroup();

        IntegralFields integralFields = new IntegralFields();

        /**
         * 如果指定了 lawName 或者 termText 参数，就以这个参数为准
         */
        if (StrUtil.isNotBlank(lawName)) {
            integralFields.setLawName(lawName);
        }

        if (StrUtil.isNotBlank(termText)) {
            integralFields.setTermText(termText);
        }

        /**
         * 如果 contentText 不为空，就说明是输入了大输入框的搜索条件
         * 当 lawName 或者 termText 参数无值时，拿这个值做默认值
         */
        if(StrUtil.isNotBlank(contentText)) {
            if(StrUtil.isBlank(integralFields.getLawName())) {
                integralFields.setLawName(contentText);
            }

            if(StrUtil.isBlank(integralFields.getTermText())) {
                integralFields.setTermText(contentText);
            }
        }

        if (StrUtil.isNotBlank(title)) {
            /** 将"第1条"变成"第一条" */
            Matcher matcher = pattern.matcher(title);
            while (matcher.find()) {
                String number = matcher.group();
                title = title.replace(number, NumberChineseFormatter.format(Long.parseLong(number), false));
            }
            integralFields.setTitle(title);
        }

        /**
         * 效力级别可以选择多个
         */
        String[] levelMultiStrArray = null;
        if (StrUtil.isNotBlank(lawLevelArrayStr)) {
            if(JSONUtil.isTypeJSON(lawLevelArrayStr)) {
                /** 是json格式 */
                levelMultiStrArray = JSONUtil.toList(lawLevelArrayStr, String.class).toArray(new String[0]);
                integralFields.setLawLevelArray(levelMultiStrArray);
            }
            else {
                /** 不是json格式 */
                levelMultiStrArray = new String[] { lawLevelArrayStr };
                integralFields.setLawLevelArray(levelMultiStrArray);
            }
        }

        /**
         * 高级搜索的查询条件
         */
        this.makeParamForCommon(documentNoMulti, publishRange, validFromRange, status,
                authority, authorityProvince, authorityCity, authorityDistrict, integralFields);

        /**
         * 判断效力基本是否有值
         */
        if (ArrayUtil.isEmpty(levelMultiStrArray)) {
            /** 未限定效力级别, 就拿全部级别 */
            levelMultiStrArray = lawCache.getLawLevelOptions().toArray(new String[0]);
        }

        for (String level : levelMultiStrArray) {
            integralFields.setLawLevel(level);

            /** 构造es查询条件 */
            SearchSourceBuilder searchSourceBuilderOfLaw = elasticSearchPortal.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralFields);

            /** 查询es */
            LawSearchHits matchProvisionHits = elasticSearchPortal.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION,
                    pageNum, pageSize, null,
                    new String[]{IntegralFields.LAW_NAME, IntegralFields.TERM_TEXT}, sortField, sortType, searchSourceBuilderOfLaw);

            /** 从查询结果中获取法律名称，并继续从es中获取历史 */
            Map<String, List<IntegralFields>> historyMap = this.makeLawProvisionHistory(matchProvisionHits, title);

            /** 从查询结果中获取法律名称，并继续从es中获取关联文件 */
            Map<Long, List<IntegralFields>> associatedFileMap = this.makeAssociatedFiles(matchProvisionHits);

            IntegralHits integralHits = new IntegralHits();
            integralHits.setHits(matchProvisionHits);
            integralHits.setHistoryMap(historyMap);
            integralHits.setAssociatedFileMap(associatedFileMap);

            lawSearchHitsGroup.putLaw(level, integralHits);
        }


        integralFields.setLawLevel(null);
        Statistics statistics = new Statistics();
        /** 统计不同状态下的匹配总数 */
        List<StatisticsRecord> statusCountList = elasticSearchPortal.countGroupByStatus(ElasticSearchPortal.INDEX__LAW_PROVISION, integralFields);
        statistics.put(IntegralFields.STATUS, statusCountList);

        /** 统计不同效力级别下的匹配总数 */
        List<StatisticsRecord> lawLevelCountList = elasticSearchPortal.countGroupByLawLevel(ElasticSearchPortal.INDEX__LAW_PROVISION, integralFields);
        statistics.put(IntegralFields.LAW_LEVEL, lawLevelCountList);

        /** 统计不同制定机关所在省级别下的匹配总数 */
        List<StatisticsRecord> provinceCountList = elasticSearchPortal.countGroupByAuthorityProvince(ElasticSearchPortal.INDEX__LAW_PROVISION, integralFields);
        statistics.put(IntegralFields.AUTHORITY_PROVINCE, provinceCountList);
        lawSearchHitsGroup.setStatistics(statistics);

        return R.ok(lawSearchHitsGroup);
    }




    /**
     * 自动提示
     *
     * [已测]http://localhost:8080/structured-law/portal/suggest?field=["law_name"]&text=中国
     *
     * @param text
     * @return
     */
    @ApiOperation(value="补全提示, 附带了条款id和法律id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.FIELD, value = "这个属性决定了提示数据的来源，[\"law_name\", \"authority\", \"title\", \"term_text\"]",
                    dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = Constants.TEXT, value = "需要被提示内容", dataType = "String", dataTypeClass = String.class)
    })
    @GetMapping("/suggest")
    public R<Set<SuggestHits>> suggest(@RequestParam(Constants.FIELD) String fieldStr, @RequestParam(Constants.TEXT) String text) {
        if (StrUtil.isBlank(fieldStr) || StrUtil.isBlank(text)) {
            return R.ok(new HashSet<>());
        }

        String[] fieldArray;
        if(JSONUtil.isTypeJSON(fieldStr)) {
            fieldArray = JSONUtil.toList(fieldStr, String.class).toArray(new String[0]);
        }
        else {
            fieldArray = new String[]{ fieldStr };
        }

        Set<SuggestHits> suggestions = new HashSet<>();
        for(String suggestField : fieldArray) {
            suggestField = suggestField + ".suggest";
            List<SuggestHits> textSuggest = elasticSearchPortal.suggest(ElasticSearchPortal.INDEX__LAW_PROVISION, suggestField, text,
                    new String[]{ IntegralFields.PROVISION_ID, IntegralFields.LAW_ID }, null);
            suggestions.addAll(textSuggest);
        }

        return R.ok(suggestions);
    }


    /**
     * 把mysql中的数据导入到elasticsearch
     *
     * @return
     */
    @Async
    @PutMapping("/init")
    public AjaxResult init() {
        /**
         * 1.如果键不存在则新增,存在则不改变已经有的值。
         * 2.存在返回 false，不存在返回 true。
         */
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_ID_BUILD_ES_INDEX, "lock",
                20 * 60 * 1000, TimeUnit.MILLISECONDS);
        if (success == null || !success) {
            /**
             * 异步进行，这段提示并不显示
             * 打开 redis-cli  执行 keys * 可看到锁
             */
            return success("索引建立正在进行...");
        }

        try {
            elasticSearchPortal.initAllIndex();
            Thread.sleep(5000);
            elasticSearchPortal.importDataToAllIndex();
            return success();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            redisTemplate.delete(LOCK_ID_BUILD_ES_INDEX);
        }
    }

    /**
     * 删除索引
     *
     * @return
     */
    @PutMapping("/deleteIndex")
    public AjaxResult deleteIndex() {
        try {
            elasticSearchPortal.deleteAllIndex();
        } catch (Exception e) {
            logger.error("", e);
            return error(e.getMessage());
        }
        return success();
    }


    //TODO 法律名称+法律条目  返回条目内容


    /**
     * @param command
     * @return
     */
    private String[] cmd(String command) {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.indexOf("windows") > -1 ? new String[]{"cmd", "/c", command} : new String[]{"/bin/sh", "-c", command};
    }

    /**
     * 记得授权 root 远程登录
     *
     * @return
     */
    @PutMapping("/backup")
    public AjaxResult backup() {
        String pattern="jdbc:(?<type>[a-z]+)://(?<host>[a-zA-Z0-9-//.]+):(?<port>[0-9]+)/(?<database>[a-zA-Z0-9_]+)?";
        Pattern namePattern = Pattern.compile(pattern);
        Matcher dateMatcher = namePattern.matcher(jdbcUrl);

        String dbHost = null;
        String dbPort = null;
        String databaseName = null;
        while (dateMatcher.find()) {
            dbHost = dateMatcher.group("host");
            dbPort = dateMatcher.group("port");
            databaseName = dateMatcher.group("database");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");
        String strNowTime = dtf.format(LocalDateTime.now());
        String fileName = strNowTime;

        if (!fileName.endsWith(".sql")) {
            fileName += ".sql";
        }

        File saveFile = new File(backupPath);
        if (!saveFile.exists()) {
            // 如果目录不存在, 则创建文件夹
            saveFile.mkdirs();
        }
        if (!backupPath.endsWith(File.separator)) {
            backupPath = backupPath + File.separator;
        }

        //拼接命令行的命令
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mysqldump").append(" --opt").append(" -h").append(dbHost).append(" -P").append(dbPort);
        stringBuilder.append(" --user=").append(dbUsername).append(" --password=").append(dbPassword)
                .append(" --lock-all-tables=false");
        stringBuilder.append(" --result-file=").append(backupPath + fileName).append(" --default-character-set=utf8 ")
                .append(databaseName);

        Process process = null;
        try {
            //调用外部执行exe文件的javaAPI
            process = Runtime.getRuntime().exec(cmd(stringBuilder.toString()));
            if (process.waitFor() != 0) {
                // 0 表示线程正常终止。
                logger.error(ArrayUtil.join(cmd(stringBuilder.toString()), ""));
                throw new IllegalStateException("backup failed");
            }
            return success(fileName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 同步爬虫库数据到mysql
     *
     * @return
     */
    @Async
    @PutMapping("/sync")
    public AjaxResult sync() {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(LOCK_ID_PARSE_MD, "lock",
                20 * 60 * 1000, TimeUnit.MILLISECONDS);
        if (success == null || !success) {
            /**
             * 异步进行，这段提示并不显示
             * 打开 redis-cli  执行 keys * 可看到锁
             */
            return success("解析建立正在进行...");
        }

        try {
            String result = HttpUtil.get(String.format("http://%s:%s/sync", syncLawApiHost, syncLawApiPort));
            return success(result);
        } finally {
            redisTemplate.delete(LOCK_ID_PARSE_MD);
        }
    }

    /**
     * 同步爬虫库数据到mysql
     *
     * @return
     */
    @PutMapping("/sync-progress")
    public AjaxResult syncProgress() {
        String result = HttpUtil.get(String.format("http://%s:%s/progress", syncLawApiHost, syncLawApiPort));
        return success(result);
    }
}
