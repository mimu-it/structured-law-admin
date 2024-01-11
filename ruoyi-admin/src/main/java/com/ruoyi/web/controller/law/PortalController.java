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
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.resp.LawDetail;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchConditionOptions;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHits;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchHitsGroup;
import com.ruoyi.web.controller.law.cache.LawCache;
import com.ruoyi.web.controller.law.srv.ElasticSearchPortal;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 */
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
    @GetMapping("/wild-search-law")
    public AjaxResult wildSearchLaw(@RequestParam(Constants.PAGE_NUM) int pageNum,
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

        return success(matchLawHits);
    }


    /**
     * http://localhost:8080/structured-law/portal/wild-search-provision?page_num=1&page_size=10&title=第四条
     * http://localhost:8080/structured-law/portal/wild-search-provision?page_num=1&page_size=10&title=第5条
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
    @GetMapping("/wild-search-provision")
    public AjaxResult wildSearchLawProvision(@RequestParam(Constants.PAGE_NUM) int pageNum,
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

        return success(matchProvisionHits);
    }

    /**
     * http://localhost:8080/structured-law/portal/wild-search-associate?page_num=1&page_size=10&law_id=8&term_text=草案
     * @param pageNum
     * @param pageSize
     * @param lawId
     * @param termText
     * @return
     */
    @GetMapping("/wild-search-associate")
    public AjaxResult wildSearchAssociateFile(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                             @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                             @RequestParam(value = IntegralFields.LAW_ID, required = false) String lawId,
                                             @RequestParam(value = IntegralFields.TERM_TEXT, required = false) String termText) {
        IntegralFields integralFields = new IntegralFields();
        if (StrUtil.isNotBlank(lawId)) {
            integralFields.setLawId(Long.parseLong(lawId));
        }

        if (StrUtil.isNotBlank(termText)) {
            integralFields.setTermText(termText);
        }

        SearchSourceBuilder searchSourceBuilderOfProvision = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE, integralFields);
        LawSearchHits matchAssociatedFileHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_ASSOCIATED_FILE,
                pageNum, pageSize, null,
                new String[]{ IntegralFields.CONTENT_TEXT }, null, searchSourceBuilderOfProvision);

        return success(matchAssociatedFileHits);
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
    @GetMapping("/search-law-provision")
    public AjaxResult searchLawProvision(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                         @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                         @RequestParam(Constants.CONDITION) String condition) {
        pageNum = pageNum < 1 ? 1 : pageNum;

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW_PROVISION, integralProvision);
        LawSearchHits matchHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION,
                pageNum, pageSize, null,
                new String[]{IntegralFields.TERM_TEXT}, IntegralFields.TITLE_NUMBER, searchSourceBuilder);
        return success(matchHits);
    }

    /**
     * 使用 elasticsearch 搜索法律
     *
     * @param pageNum
     * @param pageSize
     * @param condition
     * @return
     */
    @GetMapping("/search-law")
    public AjaxResult searchLaw(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                @RequestParam(Constants.CONDITION) String condition) {
        pageNum = pageNum < 1 ? 1 : pageNum;

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(ElasticSearchPortal.INDEX__LAW, integralProvision);
        LawSearchHits matchHits = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                pageNum, pageSize, null,
                new String[]{IntegralFields.TERM_TEXT}, null, searchSourceBuilder);
        return success(matchHits);
    }

    /**
     * 获取法律详情
     * http://localhost:8080/structured-law/portal/law-content?law_id=110
     *
     * @param lawId
     * @param size
     * @return
     */
    @GetMapping("/law-content")
    public AjaxResult showLawContent(@RequestParam(IntegralFields.LAW_ID) long lawId,
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
            return success(new ArrayList<>());
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

        return success(lawDetail);
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
     *
     * @param lawName
     * @param size
     * @return
     */
    @GetMapping("/law-history")
    public AjaxResult listLawHistory(@RequestParam(IntegralFields.LAW_NAME) String lawName,
                                     @RequestParam(value = Constants.SIZE, required = false) Integer size) {
        List<IntegralFields> matchList = elasticSearchSrv.listLawHistory(lawName, size, new String[]{
                IntegralFields.LAW_NAME,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM
        });
        return success(matchList);
    }


    /**
     * 获取各个条件的选项
     *
     * @return
     */
    @GetMapping("/conditionsOptions")
    public AjaxResult conditionsOptions() {
        LawSearchConditionOptions options = new LawSearchConditionOptions();
        options.setLevelOptions(lawCache.getLawLevelOptions());
        options.setAuthorityOptions(lawCache.getAuthorityOptions());
        options.setStatusOptions(lawCache.getStatusOptions());

        //collect_status 不知对应什么
        return success(options);
    }

    /**
     * 如果未限定level，就分组查询
     * 限定了level,就按指定级别查询
     * <p>
     * http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&authority=全国人民代表大会常务委员会
     * http://localhost:8080/structured-law/portal/group/search-law?page_num=1&page_size=10&authority=全国人民代表大会常务委员会&law_level=%E6%B3%95%E5%BE%8B%E8%A7%A3%E9%87%8A
     *
     * @param pageNum
     * @param pageSize
     * @param lawLevel
     * @param authority
     * @return
     */
    @GetMapping("/group/search-law")
    public AjaxResult searchGroupByLawLevel(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                            @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                            @RequestParam(value = IntegralFields.LAW_LEVEL, required = false) String lawLevel,
                                            @RequestParam(value = IntegralFields.AUTHORITY, required = false) String authority) {
        LawSearchHitsGroup lawSearchHitsGroup = new LawSearchHitsGroup();

        IntegralFields integralFields = new IntegralFields();
        integralFields.setAuthority(authority);

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
        return success(lawSearchHitsGroup);
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
            elasticSearchSrv.importDataToAllIndex();
            return success();
        } finally {
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
     * @param text
     * @return
     */
    @GetMapping("/suggest-multi")
    public AjaxResult suggestMulti(@RequestParam(Constants.TEXT) String text) {
        if (StrUtil.isBlank(text)) {
            return success();
        }

        List<String> suggestions = new ArrayList<>();
        List<String> authoritySuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, IntegralFields.AUTHORITY, text, null, null);
        suggestions.addAll(authoritySuggest);

        List<String> lawNameSuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, IntegralFields.LAW_NAME, text, null, null);
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
     * @param text
     * @return
     */
    @GetMapping("/suggest")
    public AjaxResult suggest(@RequestParam(Constants.FIELD) String field, @RequestParam(Constants.TEXT) String text) {
        if (StrUtil.isBlank(field) || StrUtil.isBlank(text)) {
            return success();
        }
        return success(elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW_PROVISION, field, text, null, null));
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
