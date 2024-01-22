package com.ruoyi.web.controller.law.cache;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.file.JarFileReaderUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.law.api.domain.inner.AuthorityTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiao.hu
 * @date 2024-01-08
 * @apiNote
 */
@Service
public class LawCache {

    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    /**
     * 项目启动时，初始化数据到redis
     */
    @PostConstruct
    public void init() {
        this.clearConditionOptionsCache();
        this.loadConditionOptions();
    }

    /**
     * 把查询选项都缓存到redis中
     */
    private void loadConditionOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        List<String> lawLevelOptions = slLawCategoryService.listLawLevel();
        redisCache.setCacheObject(getConditionOptionsCacheKey(SlLaw.LAW_LEVEL), lawLevelOptions);

        List<SlLaw> authorityOptions = slLawService.listAuthority();
        redisCache.setCacheObject(getConditionOptionsCacheKey(SlLaw.AUTHORITY), authorityOptions);

        List<Integer> statusOptions = slLawService.listStatus();
        redisCache.setCacheObject(getConditionOptionsCacheKey(SlLaw.STATUS), statusOptions);

        List<AuthorityTreeNode> authorityTree = this.initAuthorityTree();
        redisCache.setCacheObject(getConditionOptionsCacheKey(Constants.AUTHORITY_TREE), authorityTree);
        //TODO 征集状态是啥
    }

    /**
     * 清空结构化法条所使用的查询条件选项缓存
     */
    public void clearConditionOptionsCache() {
        Collection<String> keys = SpringUtils.getBean(RedisCache.class).keys(CacheConstants.STRUCTURED_LAW_CONDITION_OPTIONS_KEY + "*");
        SpringUtils.getBean(RedisCache.class).deleteObject(keys);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getConditionOptionsCacheKey(String configKey) {
        return CacheConstants.STRUCTURED_LAW_CONDITION_OPTIONS_KEY + configKey;
    }

    /**
     * 获取效力级别目录
     * @return
     */
    public List<String> getLawLevelOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        return redisCache.getCacheObject(getConditionOptionsCacheKey(SlLaw.LAW_LEVEL));
    }

    /**
     * 获取制定机关选项
     * @return
     */
    public List<String> getAuthorityOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        return redisCache.getCacheObject(getConditionOptionsCacheKey(SlLaw.AUTHORITY));
    }

    /**
     * 初始化制定机关的树
     * @return
     */
    private List<AuthorityTreeNode> initAuthorityTree() {
        try {
            String configStr = readConfig("authority/tree.json");
            return JSONUtil.toBean(configStr, new TypeReference<List<AuthorityTreeNode>>() {}, false);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 读取制定机关的树
     * @return
     */
    public List<AuthorityTreeNode> getAuthorityTree() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        return redisCache.getCacheObject(getConditionOptionsCacheKey(Constants.AUTHORITY_TREE));
    }

    /**
     * 读取jar中的配置文件
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readConfig(String filePath) throws IOException {
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
     * 获取状态选项
     * @return
     */
    public List<String> getStatusOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        List<Integer> statusList = redisCache.getCacheObject(getConditionOptionsCacheKey(SlLaw.STATUS));
        return statusList.stream().map((statusNumber) -> String.valueOf(statusNumber)).collect(Collectors.toList());
    }

    /**
     * TODO 放入数据字典
     * @return
     */
    public Map<Integer, String> getStatusOptionsMap() {
        Map<Integer, String> map = new HashMap<>(4);
        map.put(1, "有效");
        map.put(3, "尚未生效");
        map.put(5, "已修改");
        map.put(9, "已废止");
        map.put(7, "未知");
        map.put(0, "无");
        return map;
    }
}
