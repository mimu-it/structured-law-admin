package com.ruoyi.web.controller.law.srv;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
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
     * @param lawIdList
     * @return
     */
    @DataSource(DataSourceType.SLAVE)
    public List<SlLaw> getIncrementalLaw(List<Long> lawIdList) {
        if (lawIdList == null) {
            return slLawService.selectSlLawList(new SlLaw());
        }

        return slLawService.getByIds(lawIdList, new String[]{
                SlLaw.ID,
                SlLaw.STATUS,
                SlLaw.VER,
                SlLaw.PUBLISH,
                SlLaw.LAW_LEVEL,
                SlLaw.AUTHORITY,
                SlLaw.NAME,
                SlLaw.DOCUMENT_NO,
                SlLaw.AUTHORITY_DISTRICT,
                SlLaw.AUTHORITY_CITY,
                SlLaw.AUTHORITY_PROVINCE,
                SlLaw.VALID_FROM,
                SlLaw.CATEGORY_ID,
                SlLaw.PREFACE,
                SlLaw.TAGS,
                SlLaw.SUBTITLE,
                SlLaw.DOCUMENT_TYPE,
                SlLaw.LAW_ORDER,
                SlLaw.FULL_CONTENT,
                SlLaw.ORIGINAL_ID
        });
    }

    /**
     * 获取目录数据
     * @param categoryId
     * @return
     */
    @DataSource(DataSourceType.SLAVE)
    public SlLawCategory getIncrementalLawCategory(long categoryId) {
        return slLawCategoryService.getById(categoryId, new String[]{
                SlLawCategory.FOLDER,
                SlLawCategory.NAME,
                SlLawCategory.CATEGORY_ORDER,
                SlLawCategory.CATEGORY_GROUP,
                SlLawCategory.IS_SUB_FOLDER
        });
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


    private void insertLaw(SlLaw law) {
        int newLawId = slLawService.insertSlLaw(law);
        this.insertCategory(law.getCategoryId());
    }

    private void insertCategory(long categoryId) {
        SlLawCategory newSlLawCategory = this.
    }
}
