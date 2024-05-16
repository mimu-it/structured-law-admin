package com.ruoyi.web.controller.law.srv.incremental;

import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.system.domain.SlAssociatedFile;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlAssociatedFileService;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.system.service.ISlLawService;
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
     *
     * @param lawList
     */
    @Transactional(rollbackFor = Exception.class)
    public void merge(List<SlLaw> lawList) {
        for(SlLaw law : lawList) {
            SlLaw condition = new SlLaw();
            condition.setPublish(law.getPublish());
            condition.setName(law.getName());
            condition.setLawLevel(law.getLawLevel());
            condition.setAuthority(law.getAuthority());

            Integer status = law.getStatus();

            List<SlLaw> existingLawList = slLawService.selectLawList(condition, new String[]{
                    SlLaw.ID,
                    SlLaw.STATUS
            });

            if(existingLawList.isEmpty()) {
                // 全部新增
                this.insertLaw(law);
                continue;
            }

            if(existingLawList.size() > 1) {
                throw new IllegalStateException("存在多部相同的法律");
            }

            SlLaw existingLaw = existingLawList.get(0);
            if(status.equals(existingLaw.getStatus())) {
                /** 如果状态也一样，就忽略 */
                continue;
            }

            SlLaw conditionUpdate = new SlLaw();
            conditionUpdate.setStatus(status);
            conditionUpdate.setId(existingLaw.getId());
            int effectCount = slLawService.updateSlLaw(conditionUpdate);
            if(effectCount != 1) {
                throw new IllegalStateException("Law更新状态失败");
            }
        }
    }

    /**
     * 新增数据入库
     * @param law
     */
    private void insertLaw(SlLaw law) {
        long categoryId = this.insertCategory(law.getCategoryId());
        // 更新为新的 categoryId
        law.setCategoryId(categoryId);
        int newLawId = slLawService.insertSlLaw(law);
        this.insertProvision(law.getId(), newLawId);
        this.insertAssociatedFile(law.getId(), newLawId);
    }

    /**
     * 有必要的话新增Category
     * @param categoryId
     * @return
     */
    private long insertCategory(long categoryId) {
        SlLawCategory newSlLawCategory = incrementalDataSrv.getIncrementalLawCategory(categoryId);

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
        }
    }
}
