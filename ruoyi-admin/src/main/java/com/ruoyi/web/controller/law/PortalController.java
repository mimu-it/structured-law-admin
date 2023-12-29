package com.ruoyi.web.controller.law;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.github.pagehelper.Page;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralProvision;
import com.ruoyi.web.controller.law.srv.ElasticSearchSrv;
import com.ruoyi.web.controller.law.srv.PortalSrv;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2023-12-25
 * @apiNote
 */
@RestController
@RequestMapping("/structured-law/portal")
public class PortalController extends BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private PortalSrv portalSrv;

    @Resource
    private ElasticSearchSrv elasticSearchSrv;

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

    /**
     * 使用elasticsearch 搜索
     * @param pageNum
     * @param condition
     * @return
     */
    @GetMapping("/search")
    public AjaxResult search(@RequestParam(Constants.PAGE_NUM) int pageNum, @RequestParam(Constants.CONDITION) String condition) {
        if(pageNum < 1) {
            pageNum = 1;
        }

        IntegralProvision integralProvision = JSONUtil.toBean(condition, IntegralProvision.class);

        SearchResponse<IntegralProvision> searchResponse = elasticSearchSrv.searchByPage(pageNum, integralProvision);

        TotalHits total = searchResponse.hits().total();
        List<Hit<IntegralProvision>> list = searchResponse.hits().hits();
        List<IntegralProvision> integralProvisionList = new ArrayList<>(list.size());
        for(Hit<IntegralProvision> hit : list) {
            IntegralProvision provision = hit.source();

            Map<String, List<String>> highlightMap =  hit.highlight();
            //System.out.println(highlightMap);
            String termTextKey = StrUtil.toCamelCase(IntegralProvision.TERM_TEXT);
            if(highlightMap.get(termTextKey) != null) {
                List<String> termTextList = highlightMap.get(termTextKey);
                provision.setTermText(CollectionUtil.join(termTextList, ""));
            }

            integralProvisionList.add(provision);
        }
        return success(integralProvisionList);
    }

    /**
     * 获取各个条件的选项
     * @return
     */
    @GetMapping("/conditionsOptions")
    public AjaxResult conditionsOptions() {
        // 存储数据
        redisTemplate.opsForValue().set("test_key", "test_value");
        // 获取数据
        String value = (String) redisTemplate.opsForValue().get("test_key");
        return success(value);
    }

    /**
     * 把mysql中的数据导入到elasticsearch
     * @return
     */
    @PutMapping("/init")
    public AjaxResult init() {
        elasticSearchSrv.initIndex();

        int pageNum = 0;
        int pageSize = 50;
        Page<IntegralProvision> page = new Page<>();
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
                elasticSearchSrv.bulkInsert(page.getResult());
            }

            /** 查询当前页的数据 */
            pageNum++;
            page = portalSrv.listIntegralProvisionsByPage(pageNum, pageSize);
        }

        return success();
    }

    /**
     * 删除索引
     * @return
     */
    @PutMapping("/deleteIndex")
    public AjaxResult deleteIndex() {
        try {
            elasticSearchSrv.deleteIndexOfLaw();
        } catch (IOException e) {
            logger.error("", e);
            return error(e.getMessage());
        }
        return success();
    }


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
    @PutMapping("/sync")
    public AjaxResult sync() {
        String result = HttpUtil.get(String.format("http://%s:%s/sync", syncLawApiHost, syncLawApiPort));
        return success(result);
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
