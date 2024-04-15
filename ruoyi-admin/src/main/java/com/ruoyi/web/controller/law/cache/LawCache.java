package com.ruoyi.web.controller.law.cache;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.law.api.domain.inner.TreeNode;
import com.ruoyi.web.controller.law.values.LawStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
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

        List<SlLaw> authorityOptions = slLawService.listAuthority(null, null);
        redisCache.setCacheObject(getConditionOptionsCacheKey(SlLaw.AUTHORITY), authorityOptions);

        List<Integer> statusOptions = slLawService.listStatus();
        redisCache.setCacheObject(getConditionOptionsCacheKey(SlLaw.STATUS), statusOptions);

        List<TreeNode> authorityTree = this.initAuthorityTree();
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
    private List<TreeNode> initAuthorityTree() {
        try {
            String configStr = readConfig("authority/tree.json");
            List<TreeNode> orgList = JSONUtil.toBean(configStr, new TypeReference<List<TreeNode>>() {}, false);
            for(TreeNode org : orgList) {
                dfsIterative(org);
            }

            return orgList;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 便利 tree ，对里面的数据进行初始化
     * @param root
     */
    private void dfsIterative(TreeNode root) {
        if (root == null) {
            return;
        }
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode current = stack.pop();

            /**
             * authority/tree.json 已经构建了 org 与 province 的关系，所以不用关心 province 这块的数据
             */
            if("province".equals(current.getNodeType())) {
                if(StrUtil.isBlank(current.getLabel())) {
                    /** 数据存在 current.getLabel() 为空的 */
                    continue;
                }

                /** 如果父是province，就找该 province 下的city */
                List<SlLaw> cityList = slLawService.listCity(current.getLabel());
                if(cityList != null) {
                    /** 给 province 增加子节点 */
                    List<TreeNode> children = current.getChildren();
                    if(children == null) {
                        children = new ArrayList<>();
                        current.setChildren(children);
                    }

                    for(SlLaw cityHolder : cityList) {
                        if(StrUtil.isNotBlank(cityHolder.getAuthorityCity())) {
                            TreeNode cityNode = new TreeNode();
                            cityNode.setLabel(cityHolder.getAuthorityCity());
                            cityNode.setNodeType("city");

                            makeCurrentAsFakeParentForShow(current, cityNode);
                            children.add(cityNode);
                        }
                    }
                }
            }
            else if("city".equals(current.getNodeType())) {
                if(StrUtil.isBlank(current.getLabel())) {
                    /** 数据存在 current.getLabel() 为空的 */
                    continue;
                }

                /** 如果父是city，就找authority*/
                List<SlLaw> authorityList = slLawService.listAuthority(null, current.getLabel());
                if(authorityList != null) {
                    List<TreeNode> children = current.getChildren();
                    if(children == null) {
                        children = new ArrayList<>();
                        current.setChildren(children);
                    }

                    for(SlLaw authorityHolder : authorityList) {
                        if(StrUtil.isNotBlank(authorityHolder.getAuthority())) {
                            TreeNode cityNode = new TreeNode();
                            cityNode.setLabel(authorityHolder.getAuthority());
                            cityNode.setNodeType("authority");

                            //为了避免循环引用，导致json输出时发生异常level too large : 2048，就复制一下parent
                            makeCurrentAsFakeParentForShow(current, cityNode);
                            children.add(cityNode);
                        }
                    }
                }
            }

            if(current.getChildren() != null) {
                // 将子节点压入栈中，以便后续遍历
                List<TreeNode> list = current.getChildren();
                for (int i = list.size() - 1; i >= 0; i--) {
                    TreeNode child = list.get(i);

                    if(child.getParent() == null) {
                        makeCurrentAsFakeParentForShow(current, child);
                    }

                    stack.push(child);
                }
            }
        }
    }

    /**
     * 如果使用真正的parent，会附带 children 指向自己，从而引发循环引用，
     * 导致json输出时发生异常level too large : 2048，就复制一下parent
     * 所以就复制一个不带children的假父节点用于页面显示
     * @param current
     * @param cityNode
     */
    private void makeCurrentAsFakeParentForShow(TreeNode current, TreeNode cityNode) {
        TreeNode parentNodeCopy = new TreeNode();
        parentNodeCopy.setLabel(current.getLabel());
        parentNodeCopy.setNodeType(current.getNodeType());
        cityNode.setParent(parentNodeCopy);
    }

    /**
     * 读取制定机关的树
     * @return
     */
    public List<TreeNode> getAuthorityTree() {
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
        for (LawStatus lawStatus : LawStatus.values()) {
            map.put(lawStatus.getKey(), lawStatus.getValue());
        }

        /*map.put(1, "有效");
        map.put(3, "尚未生效");
        map.put(5, "已修改");
        map.put(9, "已废止");
        map.put(7, "未知");
        map.put(0, "无");*/
        return map;
    }
}
