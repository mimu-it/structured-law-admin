package com.ruoyi.web.controller.law.cache;

import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
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
     * 获取状态选项
     * @return
     */
    public List<String> getStatusOptions() {
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        List<Integer> statusList = redisCache.getCacheObject(getConditionOptionsCacheKey(SlLaw.STATUS));
        return statusList.stream().map((statusNumber) -> String.valueOf(statusNumber)).collect(Collectors.toList());
    }
}
