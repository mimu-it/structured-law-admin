package com.ruoyi.web.controller.law.srv;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralProvision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 */
@Service
public class PortalSrv {

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private ISlLawService slLawService;


    /**
     * 项目启动时，初始化数据到redis
     */
    @PostConstruct
    public void init() {
        this.clearConditionOptionsCache();
        this.loadConditionOptions();
    }

    /**
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<IntegralProvision> listIntegralProvisionsByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize, null);

        SlLawProvision slLawProvision = new SlLawProvision();
        Page<SlLawProvision> list = (Page<SlLawProvision>) slLawProvisionService.selectSlLawProvisionList(slLawProvision);

        List<IntegralProvision> integralProvisionList = new ArrayList<>(list.size());

        LFUCache<Long, SlLaw> cacheLaw = CacheUtil.newLFUCache(10);
        LFUCache<Long, SlLawCategory> cacheCategory = CacheUtil.newLFUCache(10);
        for(SlLawProvision row : list) {
            IntegralProvision integralProvision = new IntegralProvision();
            integralProvision.setLawId(row.getLawId());
            integralProvision.setProvisionId(row.getId());
            integralProvision.setTitle(row.getTitle());
            integralProvision.setTitleNumber(row.getTitleNumber());
            integralProvision.setTermText(row.getTermText());

            if(ObjectUtil.isNotNull(row.getLawId())) {
                SlLaw law = cacheLaw.get(row.getLawId());
                if(law == null) {
                    law = slLawService.getById(row.getLawId(), new String[]{
                            SlLaw.CATEGORY_ID,
                            SlLaw.NAME,
                            SlLaw.LAW_TYPE,
                            SlLaw.AUTHORITY,
                            SlLaw.PUBLISH,
                            SlLaw.STATUS,
                            SlLaw.SUBTITLE,
                            SlLaw.VALID_FROM,
                            SlLaw.TAGS,
                            SlLaw.PREFACE
                    });
                    cacheLaw.put(row.getLawId(), law);
                }

                integralProvision.setLawName(law.getName());
                integralProvision.setLawType(law.getLawType());
                integralProvision.setAuthority(law.getAuthority());
                integralProvision.setPublish(law.getPublish());
                integralProvision.setStatus(law.getStatus());
                integralProvision.setSubtitle(law.getSubtitle());
                integralProvision.setValidFrom(law.getValidFrom());
                integralProvision.setTags(law.getTags());
                integralProvision.setPreface(law.getPreface());

                SlLawCategory category = cacheCategory.get(law.getCategoryId());
                if(category == null) {
                    category = slLawCategoryService.getById(law.getCategoryId(), new String[]{
                            SlLawCategory.FOLDER
                    });
                    cacheCategory.put(law.getCategoryId(), category);
                }

                integralProvision.setFolder(category.getFolder());
            }

            integralProvisionList.add(integralProvision);
        }

        Page<IntegralProvision> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(integralProvisionList);

        return newPage;
    }


    /**
     * 把查询选项都缓存到redis中
     */
    private void loadConditionOptions() {
        List<String> lawTypeOptions = slLawCategoryService.listLawType();
        SpringUtils.getBean(RedisCache.class).setCacheObject(getConditionOptionsCacheKey(SlLaw.LAW_TYPE), lawTypeOptions);

        List<String> authorityOptions = slLawService.listAuthority();
        SpringUtils.getBean(RedisCache.class).setCacheObject(getConditionOptionsCacheKey(SlLaw.AUTHORITY), authorityOptions);

        List<Integer> statusOptions = slLawService.listStatus();
        SpringUtils.getBean(RedisCache.class).setCacheObject(getConditionOptionsCacheKey(SlLaw.STATUS), statusOptions);

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
}
