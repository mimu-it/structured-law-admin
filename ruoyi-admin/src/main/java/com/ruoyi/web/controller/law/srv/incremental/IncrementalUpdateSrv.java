package com.ruoyi.web.controller.law.srv.incremental;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.ImportTraceConstants;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.ImportTrace;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-05-16
 * @apiNote
 */
@Service
public class IncrementalUpdateSrv {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlAssociatedFileService slAssociatedFileService;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    @Autowired
    private IncrementalDataSrv incrementalDataSrv;

    @Autowired
    private ISlIncrementalLogService incrementalLogService;

    /**
     * 必须清空关联数据才能完成删除
     *
     * @param ids
     * @return
     */
    public int removeLaw(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (slLawProvisionService.count(id) == 0 && slAssociatedFileService.count(id) == 0) {
                count = count + slLawService.deleteSlLawByIds(ids);
            }
        }
        return count;
    }

    /**
     * 合并增量数据入库
     *
     * 除了更改状态，不会改变原法律数据
     * @param lawList
     */
    @Transactional(rollbackFor = Exception.class)
    public void merge(List<SlLaw> lawList) {
        ImportTrace.prepare();
        for(SlLaw law : lawList) {
            long incrementalId = law.getId();
            String lawName = law.getName();

            /**
             * 根据固定特征获取原库中获取法律信息
             */
            SlLaw condition = new SlLaw();
            condition.setPublish(law.getPublish());
            condition.setName(lawName);
            condition.setLawLevel(law.getLawLevel());
            condition.setAuthority(law.getAuthority());

            Integer status = law.getStatus();

            List<SlLaw> existingLawList = slLawService.selectLawListForIncrementalUpdate(condition, new String[]{
                    SlLaw.ID,
                    SlLaw.STATUS
            });

            /**
             * 如果原库不存在这条法律，就新增
             */
            if(existingLawList.isEmpty()) {
                // 全部新增
                long newId = this.insertLaw(law);
                ImportTrace.record("<p>新增了法律法规，增量表中的id是%s，入库后id是%s，名称是《%s》</p>",
                        incrementalId, newId, lawName);
                continue;
            }

            if(existingLawList.size() > 1) {
                throw new IllegalStateException("存在多部相同的法律");
            }

            /**
             * 判断状态
             */
            SlLaw existingLaw = existingLawList.get(0);
            if(status.equals(existingLaw.getStatus())) {
                /** 如果状态也一样，就忽略 */
                ImportTrace.increment(ImportTraceConstants.LAW_IGNORE_COUNT);
                ImportTrace.record("<p>忽略了法律法规，增量表中的id是%s，名称是《%s》</p>", incrementalId, lawName);
                continue;
            }

            /**
             * 更新状态
             */
            SlLaw conditionUpdate = new SlLaw();
            conditionUpdate.setStatus(status);
            conditionUpdate.setId(existingLaw.getId());
            int effectCount = slLawService.updateSlLaw(conditionUpdate);
            if(effectCount != 1) {
                throw new IllegalStateException("Law更新状态失败");
            }

            ImportTrace.increment(ImportTraceConstants.LAW_CHANGE_STATUS_COUNT);
            ImportTrace.record("<p>修改了法律法规状态，增量表中的id是%s，名称是《%s》</p>", incrementalId, lawName);
        }

        ImportTrace.record("<p>事务成功提交</p>");
        List<String> traceLog = ImportTrace.getTrace();
        int categoryInsertCount = ImportTrace.getCounter(ImportTraceConstants.CATEGORY_INSERT_COUNT);
        int lawInsertCount = ImportTrace.getCounter(ImportTraceConstants.LAW_INSERT_COUNT);
        int lawIgnoreCount = ImportTrace.getCounter(ImportTraceConstants.LAW_IGNORE_COUNT);
        int lawChangeStatusCount = ImportTrace.getCounter(ImportTraceConstants.LAW_CHANGE_STATUS_COUNT);
        int provisionInsertCount = ImportTrace.getCounter(ImportTraceConstants.PROVISION_INSERT_COUNT);
        int associatedFileInsertCount = ImportTrace.getCounter(ImportTraceConstants.ASSOCIATED_FILE_INSERT_COUNT);
        ImportTrace.clear();

        StringBuffer countLog = new StringBuffer();
        countLog.append(",").append("新增目录数量：").append(categoryInsertCount);
        countLog.append(",").append("新增法律法规数量：").append(lawInsertCount);
        countLog.append(",").append("忽略法律法规数量：").append(lawIgnoreCount);
        countLog.append(",").append("更改法律法规状态数量：").append(lawChangeStatusCount);
        countLog.append(",").append("新增法条状态数量：").append(provisionInsertCount);
        countLog.append(",").append("新增关联文件数量：").append(associatedFileInsertCount);

        String fullLog = "<p>统计：" + countLog.substring(1) + "</p>" + CollectionUtil.join(traceLog, "");
        logger.info(fullLog);
        this.log(fullLog);
    }

    /**
     * 新增数据入库
     * @param law
     */
    private long insertLaw(SlLaw law) {
        long incrementalId = law.getId();
        SlLawCategory newSlLawCategory = incrementalDataSrv.getIncrementalLawCategory(law.getCategoryId());
        long categoryId = this.insertCategory(newSlLawCategory);

        ImportTrace.record("<p>新增了法律法规目录，增量表中的id是%s，入库后id是%s，名称是《%s》</p>",
                law.getCategoryId(), categoryId, newSlLawCategory.getName());

        // 更新为新的 categoryId
        law.setCategoryId(categoryId);
        law.setId(null);
        int count = slLawService.insertSlLaw(law);
        if (count != 1) {
            throw new IllegalStateException("插入数据不成功，数据:" + JSONUtil.toJsonStr(law));
        }
        ImportTrace.increment(ImportTraceConstants.LAW_INSERT_COUNT);

        this.insertProvision(incrementalId, law.getId());
        this.insertAssociatedFile(incrementalId, law.getId());
        return law.getId();
    }

    /**
     * 有必要的话新增Category
     * @param newSlLawCategory
     * @return
     */
    private long insertCategory(SlLawCategory newSlLawCategory) {
        SlLawCategory condition = new SlLawCategory();
        condition.setName(newSlLawCategory.getName());
        List<SlLawCategory> categoryList = slLawCategoryService.listLawCategory(condition, new String[]{
                BaseEntity.ID, SlLawCategory.FOLDER, SlLawCategory.CATEGORY_ORDER, SlLawCategory.NAME,
                SlLawCategory.IS_SUB_FOLDER, SlLawCategory.CATEGORY_ORDER
        });

        if(categoryList.isEmpty()) {
            int count = slLawCategoryService.insertSlLawCategory(newSlLawCategory);
            if(count != 1) {
                throw new IllegalStateException("插入数据不成功，数据:" + JSONUtil.toJsonStr(newSlLawCategory));
            }

            ImportTrace.increment(ImportTraceConstants.CATEGORY_INSERT_COUNT);
            return newSlLawCategory.getId();
        }

        if(categoryList.size() > 1) {
            throw new IllegalStateException("存在多个相同的目录, categoryList: " + JSONUtil.toJsonStr(categoryList));
        }

        return categoryList.get(0).getId();
    }

    /**
     * 因为如果存在law，那么它的条款就不会更新
     * @param incrementalLawId
     * @param newLawId
     */
    private void insertProvision(long incrementalLawId, long newLawId) {
        List<SlLawProvision> provisionList = incrementalDataSrv.getIncrementalLawProvision(incrementalLawId);
        for(SlLawProvision p : provisionList) {
            p.setLawId(newLawId);
            int count = slLawProvisionService.insertSlLawProvision(p);
            if(count != 1) {
                throw new IllegalStateException("插入数据不成功，数据:" + JSONUtil.toJsonStr(p));
            }
            ImportTrace.increment(ImportTraceConstants.PROVISION_INSERT_COUNT);
        }
    }

    /**
     * 因为如果存在law，那么它的关联文件就不会更新
     * @param incrementalLawId
     * @param newLawId
     */
    private void insertAssociatedFile(long incrementalLawId, long newLawId) {
        List<SlAssociatedFile> associatedFiles = incrementalDataSrv.getIncrementalAssociatedFile(incrementalLawId);
        for(SlAssociatedFile p : associatedFiles) {
            p.setLawId(newLawId);
            int count = slAssociatedFileService.insertSlAssociatedFile(p);
            if(count != 1) {
                throw new IllegalStateException("插入数据不成功，数据:" + JSONUtil.toJsonStr(p));
            }
            ImportTrace.increment(ImportTraceConstants.ASSOCIATED_FILE_INSERT_COUNT);
        }
    }

    /**
     * 记录日志
     * @param fullLog
     */
    private void log(String fullLog) {
        SlIncrementalLog slIncrementalLog = new SlIncrementalLog();
        slIncrementalLog.setLogContent(fullLog);
        incrementalLogService.insertSlIncrementalLog(slIncrementalLog);
    }
}
