package com.ruoyi.web.controller.law;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import com.ruoyi.web.controller.law.api.domain.resp.LawSearchConditionOptions;
import com.ruoyi.web.controller.law.srv.ElasticSearchPortal;
import com.ruoyi.web.controller.law.srv.PortalSrv;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private PortalSrv portalSrv;

    @Resource
    private ElasticSearchPortal elasticSearchSrv;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${backup.path}")
    private String backupPath;

    @Value("${backup.db.host}")
    private String dbHost;

    @Value("${backup.db.port}")
    private String dbPort;

    @Value("${backup.db.username}")
    private String dbUsername;

    @Value("${backup.db.password}")
    private String dbPassword;

    @Value("${backup.db.database}")
    private String dbDatabase;

    @Value("${law.structured.sync.api-host}")
    private String syncLawApiHost;

    @Value("${law.structured.sync.api-port}")
    private String syncLawApiPort;


    @PostConstruct
    public void resetLock() {
        redisTemplate.delete(LOCK_ID_BUILD_ES_INDEX);
    }

    /**
     * 使用 elasticsearch 搜索条款
     *
     * http://localhost:8080/structured-law/portal/search?pageNum=1&&condition={termText:"全国"}
     *
     * @param pageNum
     * @param condition
     * @return
     */
    @GetMapping("/search-law-provision")
    public AjaxResult searchLawProvision(@RequestParam(Constants.PAGE_NUM) int pageNum,
                             @RequestParam(Constants.PAGE_SIZE) int pageSize,
                             @RequestParam(Constants.CONDITION) String condition) {
        if(pageNum < 1) {
            pageNum = 1;
        }

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        String[] fields = null;
        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(integralProvision, null, null);
        List<IntegralFields> matchList = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW_PROVISION,
                pageNum, pageSize, fields,
                IntegralFields.TERM_TEXT, IntegralFields.TITLE_NUMBER, searchSourceBuilder);
        return success(matchList);
    }

    /**
     * 使用 elasticsearch 搜索法律
     * @param pageNum
     * @param pageSize
     * @param condition
     * @return
     */
    @GetMapping("/search-law")
    public AjaxResult searchLaw(@RequestParam(Constants.PAGE_NUM) int pageNum,
                                @RequestParam(Constants.PAGE_SIZE) int pageSize,
                                @RequestParam(Constants.CONDITION) String condition) {
        if(pageNum < 1) {
            pageNum = 1;
        }

        IntegralFields integralProvision = JSONUtil.toBean(condition, IntegralFields.class);

        String[] fields = null;
        SearchSourceBuilder searchSourceBuilder = elasticSearchSrv.mustConditions(integralProvision, null, null);
        List<IntegralFields> matchList = elasticSearchSrv.searchByPage(ElasticSearchPortal.INDEX__LAW,
                pageNum, pageSize, fields,
                IntegralFields.TERM_TEXT, null, searchSourceBuilder);
        return success(matchList);
    }

    /**
     * 获取法律详情
     * @param lawId
     * @param size
     * @return
     */
    @GetMapping("/law-content")
    public AjaxResult showLawContent(@RequestParam(IntegralFields.LAW_ID) long lawId,
                                     @RequestParam(value=Constants.SIZE, required = false) Integer size) {
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

        StringBuilder sb = new StringBuilder();
        for(IntegralFields provision : matchList) {
            String title = provision.getTitle();
            int lastIdx = title.lastIndexOf("/");
            if(lastIdx != -1) {
                title = title.substring(lastIdx + 1);
            }
            sb.append(title).append(" ").append(provision.getTermText()).append("\n");
        }

        matchOne.get(0).setContentText(sb.toString());

        return success(matchOne.get(0));
    }


    /**
     *
     * @param lawName
     * @param size
     * @return
     */
    @GetMapping("/law-history")
    public AjaxResult listLawHistory(@RequestParam(IntegralFields.LAW_NAME) String lawName,
                                     @RequestParam(value=Constants.SIZE, required = false) Integer size) {
        List<IntegralFields> matchList = elasticSearchSrv.listLawHistory(lawName, size, new String[]{
                IntegralFields.LAW_NAME,
                IntegralFields.PUBLISH,
                IntegralFields.VALID_FROM
        });
        return success(matchList);
    }


    /**
     * 获取各个条件的选项
     * @return
     */
    @GetMapping("/conditionsOptions")
    public AjaxResult conditionsOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        List<String> lawTypeOptions = redisCache.getCacheObject(PortalSrv.getConditionOptionsCacheKey(SlLaw.LAW_LEVEL));
        List<String> authorityOptions = redisCache.getCacheObject(PortalSrv.getConditionOptionsCacheKey(SlLaw.AUTHORITY));
        List<Integer> statusList = redisCache.getCacheObject(PortalSrv.getConditionOptionsCacheKey(SlLaw.STATUS));
        List<String> statusOptions = statusList.stream().map((statusNumber) -> String.valueOf(statusNumber)).collect(Collectors.toList());

        LawSearchConditionOptions options = new LawSearchConditionOptions();
        options.setLevelOptions(lawTypeOptions);
        options.setAuthorityOptions(authorityOptions);
        options.setStatusOptions(statusOptions);

        //collect_status 不知对应什么
        return success(options);
    }

    /**
     * 把mysql中的数据导入到elasticsearch
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
        }
        finally {
            redisTemplate.delete(LOCK_ID_BUILD_ES_INDEX);
        }
    }

    /**
     * 删除索引
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
     * @param text
     * @return
     */
    @GetMapping("/suggest-multi")
    public AjaxResult suggestMulti(@RequestParam(Constants.TEXT) String text) {
        if(StrUtil.isBlank(text)) {
            return success();
        }

        List<String> suggestions = new ArrayList<>();
        List<String> authoritySuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, IntegralFields.AUTHORITY, text, null, null);
        suggestions.addAll(authoritySuggest);

        List<String> lawNameSuggest = elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW, IntegralFields.LAW_NAME, text, null, null);
        for(String suggest : lawNameSuggest) {
            if(suggestions.contains(suggest)) {
                continue;
            }
            suggestions.add(suggest);
        }

        return success(suggestions);
    }

    /**
     * 自动提示
     * @param text
     * @return
     */
    @GetMapping("/suggest")
    public AjaxResult suggest(@RequestParam(Constants.FIELD) String field, @RequestParam(Constants.TEXT) String text) {
        if(StrUtil.isBlank(field) || StrUtil.isBlank(text)) {
            return success();
        }
        return success(elasticSearchSrv.suggest(ElasticSearchPortal.INDEX__LAW_PROVISION, field, text, null, null));
    }


    /**
     *
     * @param command
     * @return
     */
    private String[] cmd(String command) {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.indexOf("windows") > -1 ?  new String[]{"cmd", "/c", command} : new String[]{"/bin/sh", "-c", command};
    }

    /**
     * 记得授权 root 远程登录
     * @return
     */
    @PutMapping("/backup")
    public AjaxResult backup() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");
        String strNowTime = dtf.format(LocalDateTime.now());
        String fileName = strNowTime;

        if(!fileName.endsWith(".sql")) {
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
                .append(dbDatabase);

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
            if(process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 同步爬虫库数据到mysql
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
        }
        finally {
            redisTemplate.delete(LOCK_ID_PARSE_MD);
        }
    }

    /**
     * 同步爬虫库数据到mysql
     * @return
     */
    @PutMapping("/sync-progress")
    public AjaxResult syncProgress() {
        String result = HttpUtil.get(String.format("http://%s:%s/progress", syncLawApiHost, syncLawApiPort));
        return success(result);
    }
}
