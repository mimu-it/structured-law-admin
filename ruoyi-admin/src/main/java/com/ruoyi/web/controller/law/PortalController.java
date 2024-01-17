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
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.resp.LawDetail;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchConditionOptions;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHits;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHitsGroup;
import com.ruoyi.web.controller.law.cache.LawCache;
import com.ruoyi.web.controller.law.srv.ElasticSearchPortal;
import io.swagger.annotations.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ElasticSearchPortal elasticSearchSrv;

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
     * http://localhost:8080/structured-law/portal/wild-search-law?page_num=2&page_size=10&publish=[%221990-01-01%22,%221995-01-01%22]&status=5
     * <p>
     * http://localhost:8080/structured-law/portal/wild-search-law?page_num=2&page_size=10&status=5&law_level=司法解释
     *
     * http://localhost:8080/structured-law/portal/wild-search-law?page_num=1&page_size=10&law_name=中国
     *
     * http://localhost:8080/structured-law/portal/wild-search-law?page_num=1&page_size=10&law_name=国境河流外国籍船舶管理办法
     *
     * 若依框架后端使用的响应对象AjaxResult，和Swagger存在不兼容问题，导致返回体即使使用了Swagger注解，但是Swagger接口文档中，不显示返回体的对象Swagger文档
     *
     * @param pageNum
     * @param pageSize
     * @param lawName
     * @param documentNoMulti
     * @param publishRange
     * @param validFromRange
     * @param status
     * @param level
     * @param authority
     * @param authorityProvince
     * @param authorityCity
     * @param authorityDistrict
     * @return
     */
    @ApiOperation(value="关键词查询法律")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = IntegralFields.LAW_NAME, value = "法律名称", dataType = "String", dataTypeClass = String.class),
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
    @GetMapping("/wild-search-law")
    public R<Map<String, Object>> wildSearchLaw(@RequestParam(Constants.PAGE_NUM) int pageNum,
                            @RequestParam(Constants.PAGE_SIZE) int pageSize,
                            @RequestParam(value = IntegralFields.LAW_NAME, required = false) String lawName,
                            @RequestParam(value = IntegralFields.DOCUMENT_NO, required = false) String documentNoMulti,
                            @RequestParam(value = IntegralFields.PUBLISH, required = false) String publishRange,
                            @RequestParam(value = IntegralFields.VALID_FROM, required = false) String validFromRange,
                            @RequestParam(value = IntegralFields.STATUS, required = false) Integer status,
                            @RequestParam(value = IntegralFields.LAW_LEVEL, required = false) String level,
                            @RequestParam(value = IntegralFields.AUTHORITY, required = false) String authority,
                            @RequestParam(value = IntegralFields.AUTHORITY_PROVINCE, required = false) String authorityProvince,
                            @RequestParam(value = IntegralFields.AUTHORITY_CITY, required = false) String authorityCity,
                            @RequestParam(value = IntegralFields.AUTHORITY_DISTRICT, required = false) String authorityDistrict) {
        IntegralFields integralFields = new IntegralFields();
        if (StrUtil.isNotBlank(lawName)) {
            integralFields.setLawName(lawName);
        }

        this.makeParamForCommon(documentNoMulti, publishRange, validFromRange, status,
                level, authority, authorityProvince, authorityCity, authorityDistrict, integralFields);

        SearchSourceBuilder searchSourceBuilderOfLaw = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW, integralFields);
        LawSearchHits matchLawHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                pageNum, pageSize, null,
                new String[]{IntegralFields.LAW_NAME}, null, searchSourceBuilderOfLaw);

        Map<String, List<IntegralFields>> historyMap = this.makeLawHistoryInfo(matchLawHits);
        Map<Long, List<IntegralFields>> associatedFileMap = this.makeAssociatedFileInfo(matchLawHits);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.HITS, matchLawHits);
        resultMap.put(Constants.HISTORY, historyMap);
        resultMap.put(Constants.ASSOCIATE, associatedFileMap);

        return R.ok(resultMap);
    }


    /**
     * 从查询出来的法律中对应找法律的历史
     * @param matchLawHits
     * @return
     */
    private Map<String, List<IntegralFields>> makeLawHistoryInfo(LawSearchHits matchLawHits) {
        Map<String, List<IntegralFields>> map = new HashMap<>();
        List<IntegralFields> hitsList = matchLawHits.getSearchHits();
        Set<String> lawNameList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawNameList.add(fields.getLawNameOrigin());
        }

        for(String name : lawNameList) {
            List<IntegralFields> matchHistoryList = elasticSearchSrv.listLawHistory(name, 1000, new String[]{
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
     * http://localhost:8080/structured-law/portal/wild-search-provision?page_num=1&page_size=10&title=第四条
     * http://localhost:8080/structured-law/portal/wild-search-provision?page_num=1&page_size=10&title=第5条
     *
     * http://localhost:8080/structured-law/portal/wild-search-provision?page_num=1&page_size=10&law_name=中国&title=第5条
     *
     * @param pageNum
     * @param pageSize
     * @param lawName
     * @param title
     * @param termText
     * @param documentNoMulti
     * @param publishRange
     * @param validFromRange
     * @param status
     * @param level
     * @param authority
     * @param authorityProvince
     * @param authorityCity
     * @param authorityDistrict
     * @return
     */
    @ApiOperation(value="关键词查询法律条款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
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
    @GetMapping("/wild-search-provision")
    public R<Map<String, Object>> wildSearchLawProvision(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                    @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                    @RequestParam(value = IntegralFields.LAW_NAME, required = false) String lawName,
                                    @RequestParam(value = IntegralFields.TITLE, required = false) String title,
                                    @RequestParam(value = IntegralFields.TERM_TEXT, required = false) String termText,
                                    @RequestParam(value = IntegralFields.DOCUMENT_NO, required = false) String documentNoMulti,
                                    @RequestParam(value = IntegralFields.PUBLISH, required = false) String publishRange,
                                    @RequestParam(value = IntegralFields.VALID_FROM, required = false) String validFromRange,
                                    @RequestParam(value = IntegralFields.STATUS, required = false) Integer status,
                                    @RequestParam(value = IntegralFields.LAW_LEVEL, required = false) String level,
                                    @RequestParam(value = IntegralFields.AUTHORITY, required = false) String authority,
                                    @RequestParam(value = IntegralFields.AUTHORITY_PROVINCE, required = false) String authorityProvince,
                                    @RequestParam(value = IntegralFields.AUTHORITY_CITY, required = false) String authorityCity,
                                    @RequestParam(value = IntegralFields.AUTHORITY_DISTRICT, required = false) String authorityDistrict) {
        IntegralFields integralFields = new IntegralFields();
        if (StrUtil.isNotBlank(lawName)) {
            integralFields.setLawName(lawName);
        }

        if (StrUtil.isNotBlank(title)) {
            // 创建Matcher对象并进行匹配操作
            Matcher matcher = pattern.matcher(title);
            while (matcher.find()) {
                String number = matcher.group();
                title = title.replace(number, NumberChineseFormatter.format(Long.parseLong(number), false));
            }
            integralFields.setTitle(title);
        }

        if (StrUtil.isNotBlank(termText)) {
            integralFields.setTermText(termText);
        }

        this.makeParamForCommon(documentNoMulti, publishRange, validFromRange, status,
                level, authority, authorityProvince, authorityCity, authorityDistrict, integralFields);

        SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralFields);
        LawSearchHits matchProvisionHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION,
                pageNum, pageSize, null,
                new String[]{IntegralFields.TERM_TEXT}, IntegralFields.TITLE_NUMBER, searchSourceBuilderOfProvision);

        Map<String, List<IntegralFields>> historyMap = this.makeLawHistoryInfo(matchProvisionHits);
        Map<Long, List<IntegralFields>> associatedFileMap = this.makeAssociatedFileInfo(matchProvisionHits);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constants.HITS, matchProvisionHits);
        resultMap.put(Constants.HISTORY, historyMap);
        resultMap.put(Constants.ASSOCIATE, associatedFileMap);

        return R.ok(resultMap);
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

        SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, integralFields);
        LawSearchHits matchAssociatedFileHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                pageNum, pageSize, null,
                new String[]{ IntegralFields.CONTENT_TEXT }, null, searchSourceBuilderOfProvision);

        return R.ok(matchAssociatedFileHits);
    }

    /**
     * 关联文件也一并查出来
     * @param matchLawHits
     * @return
     */
    private Map<Long, List<IntegralFields>> makeAssociatedFileInfo(LawSearchHits matchLawHits) {
        Map<Long, List<IntegralFields>> map = new HashMap<>();
        List<IntegralFields> hitsList = matchLawHits.getSearchHits();

        Set<Long> lawIdList = new HashSet<>();
        for(IntegralFields fields : hitsList) {
            lawIdList.add(fields.getLawId());
        }

        for(Long lawId : lawIdList) {
            IntegralFields param = new IntegralFields();
            param.setLawId(lawId);
            SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, param);
            LawSearchHits matchAssociatedFileHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                    1, 1000, null, null, null, searchSourceBuilderOfProvision);

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
     * @param level
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
                                    String level,
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

        if (StrUtil.isNotBlank(level)) {
            integralFields.setLawLevel(level);
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
     * 使用 elasticsearch 搜索条款
     * <p>
     * http://localhost:8080/structured-law/portal/search?page_num=1&&condition={termText:"全国"}
     *
     * @param pageNum
     * @param condition
     * @return
     */
    @ApiOperation(value="json格式关键词查询法律条款")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.CONDITION, value = "json格式查询条件(eg. condition={law_name:\"全国\"})", dataType = "String", dataTypeClass = String.class),
    })
    @GetMapping("/search-law-provision")
    public R<LawSearchHits> searchLawProvision(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                         @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                         @RequestParam(Constants.CONDITION) String condition) {
        pageNum = pageNum < 1 ? 1 : pageNum;

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralProvision);
        LawSearchHits matchHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION,
                pageNum, pageSize, null,
                new String[]{IntegralFields.TERM_TEXT}, IntegralFields.TITLE_NUMBER, searchSourceBuilder);
        return R.ok(matchHits);
    }

    /**
     * 使用 elasticsearch 搜索法律
     *
     * @param pageNum
     * @param pageSize
     * @param condition
     * @return
     */
    @ApiOperation(value="json格式关键词查询法律")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.CONDITION, value = "json格式查询条件(eg. condition={law_name:\"全国\"})", dataType = "String", dataTypeClass = String.class),
    })
    @GetMapping("/search-law")
    public R<LawSearchHits> searchLaw(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                @RequestParam(Constants.CONDITION) String condition) {
        pageNum = pageNum < 1 ? 1 : pageNum;

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW, integralProvision);
        LawSearchHits matchHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                pageNum, pageSize, null,
                new String[]{IntegralFields.TERM_TEXT}, null, searchSourceBuilder);
        return R.ok(matchHits);
    }

    /**
     * 获取法律详情
     * http://localhost:8080/structured-law/portal/law-content?law_id=110
     *
     * @param lawId
     * @param size
     * @return
     */
    @ApiOperation(value="json格式关键词查询法律及法律正文内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = IntegralFields.LAW_ID, value = "法律id", dataType = "long", dataTypeClass = Long.class),
            @ApiImplicitParam(name = Constants.SIZE, value = "显示的条款数量(eg. 1000)", dataType = "int", dataTypeClass = Integer.class),
    })
    @GetMapping("/law-content")
    public R<LawDetail> showLawContent(@RequestParam(IntegralFields.LAW_ID) long lawId,
                                     @RequestParam(value = Constants.SIZE, required = false) Integer size) {
        List<IntegralFields> matchOne = elasticSearchSrv.listByLawId(lawId, 1, new String[]{
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

        List<IntegralFields> matchList = elasticSearchSrv.listByLawId(lawId, size, new String[]{
                IntegralFields.TITLE,
                IntegralFields.TERM_TEXT
        });

        StringBuilder sb = this.joinAllProvisions(matchList);

        IntegralFields matchedItem = matchOne.get(0);
        matchedItem.setContentText(sb.toString());

        LawDetail lawDetail = new LawDetail();
        lawDetail.setId(matchedItem.getEsDocId());
        lawDetail.setAccording("");
        lawDetail.setAuthority(matchedItem.getAuthority());
        lawDetail.setContent(matchedItem.getContentText());
        lawDetail.setDocumentNo(matchedItem.getDocumentNo());
        lawDetail.setLevel(matchedItem.getLawLevel());
        if (matchedItem.getPublish() != null) {
            lawDetail.setPublishAt(DateUtil.format(matchedItem.getPublish(), "yyyy-MM-dd"));
        }

        lawDetail.setStatus(String.valueOf(matchedItem.getStatus()));
        lawDetail.setTitle(matchedItem.getLawName());

        if (matchedItem.getValidFrom() != null) {
            lawDetail.setValidFrom(DateUtil.format(matchedItem.getValidFrom(), "yyyy-MM-dd"));
        }

        return R.ok(lawDetail);
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
     * http://localhost:8080/structured-law/portal/law-history?law_name=最高人民法院关于知识产权法庭若干问题的规定
     *
     * @param lawName
     * @param size
     * @return
     */
    @ApiOperation(value="查询某个法律的历史变更")
    @ApiImplicitParams({
            @ApiImplicitParam(name = IntegralFields.LAW_NAME, value = "法律名称", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = Constants.SIZE, value = "显示的条款数量(eg. 1000)", dataType = "int", dataTypeClass = Integer.class),
    })
    @GetMapping("/law-history")
    public R<List<IntegralFields>> listLawHistory(@RequestParam(IntegralFields.LAW_NAME) String lawName,
                                     @RequestParam(value = Constants.SIZE, required = false) Integer size) {
        List<IntegralFields> matchList = elasticSearchSrv.listLawHistory(lawName, size, new String[]{
                IntegralFields.LAW_NAME,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM
        });
        return R.ok(matchList);
    }


    /**
     * 获取各个条件的选项
     *
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
     * <p>
     * http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&authority=全国人民代表大会常务委员会
     * http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&authority=全国人民代表大会常务委员会&law_level=法律解释
     *
     * @param pageNum
     * @param pageSize
     * @param lawLevel
     * @param authority
     * @return
     */
    @ApiOperation(value="如果未限定level，就分组查询， 限定了level,就按指定级别查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.PAGE_NUM, value = "页码", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页显示数量", dataType = "int", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = IntegralFields.LAW_LEVEL, value = "效力级别", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.AUTHORITY, value = "制定机关", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = IntegralFields.STATUS, value = "状态", dataType = "int", dataTypeClass = Integer.class),
    })
    @GetMapping("/group/search-law")
    public R<LawSearchHitsGroup> searchGroupByLawLevel(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                                       @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                                       @RequestParam(value = IntegralFields.LAW_LEVEL, required = false) String lawLevel,
                                                       @RequestParam(value = IntegralFields.AUTHORITY, required = false) String authority,
                                                       @RequestParam(value = IntegralFields.STATUS, required = false) Integer status) {
        LawSearchHitsGroup lawSearchHitsGroup = new LawSearchHitsGroup();

        IntegralFields integralFields = new IntegralFields();
        integralFields.setAuthority(authority);
        integralFields.setStatus(status);

        if (StrUtil.isBlank(lawLevel)) {
            /** 未限定效力级别 */
            List<String> levelList = lawCache.getLawLevelOptions();
            for (String level : levelList) {
                integralFields.setLawLevel(level);
                SearchSourceBuilder searchSourceBuilderOfLaw = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW, integralFields);
                LawSearchHits matchLawHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                        pageNum, pageSize, null,
                        new String[]{IntegralFields.LAW_NAME}, null, searchSourceBuilderOfLaw);
                lawSearchHitsGroup.putLaw(level, matchLawHits);
            }
        } else {
            /** 限定了效力级别 */
            integralFields.setLawLevel(lawLevel);
            SearchSourceBuilder searchSourceBuilderOfLaw = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW, integralFields);
            LawSearchHits matchLawHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                    pageNum, pageSize, null,
                    new String[]{IntegralFields.LAW_NAME}, null, searchSourceBuilderOfLaw);
            lawSearchHitsGroup.putLaw(lawLevel, matchLawHits);
        }

        /** 查询关联文件，关联文件都是从第1也开始查 */
        integralFields.setLawLevel(null);
        SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, integralFields);
        LawSearchHits matchAssociatedFileHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                1, pageSize, null,
                new String[]{IntegralFields.ASSOCIATED_FILE_NAME}, null, searchSourceBuilderOfProvision);
        lawSearchHitsGroup.setAssociateFile(matchAssociatedFileHits);
        return R.ok(lawSearchHitsGroup);
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
            elasticSearchSrv.initAllIndex();
            Thread.sleep(5000);
            elasticSearchSrv.importDataToAllIndex();
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
            elasticSearchSrv.deleteAllIndex();
        } catch (Exception e) {
            logger.error("", e);
            return error(e.getMessage());
        }
        return success();
    }


    /**
     * 自动提示
     *
     * http://localhost:8080/structured-law/portal/suggest-multi?field=law_name&text=黄河
     *
     * @param text
     * @return
     */
    @GetMapping("/suggest-multi")
    public AjaxResult suggestMulti(@RequestParam(Constants.TEXT) String text) {
        if (StrUtil.isBlank(text)) {
            return success();
        }

        List<String> suggestions = new ArrayList<>();
        String authoritySuggestField = IntegralFields.AUTHORITY + ".suggest";
        String lawNameSuggestField = IntegralFields.LAW_NAME + ".suggest";
        List<String> authoritySuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, authoritySuggestField, text, null, null);
        suggestions.addAll(authoritySuggest);

        List<String> lawNameSuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, lawNameSuggestField, text, null, null);
        for (String suggest : lawNameSuggest) {
            if (suggestions.contains(suggest)) {
                continue;
            }
            suggestions.add(suggest);
        }

        return success(suggestions);
    }

    /**
     * 自动提示
     *
     * http://localhost:8080/structured-law/portal/suggest?field=law_name&text=中国
     *
     * @param text
     * @return
     */
    @GetMapping("/suggest")
    public AjaxResult suggest(@RequestParam(Constants.FIELD) String field, @RequestParam(Constants.TEXT) String text) {
        if (StrUtil.isBlank(field) || StrUtil.isBlank(text)) {
            return success();
        }

        String suggestField = field + ".suggest";
        return success(elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW_PROVISION, suggestField, text, null, null));
    }


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
