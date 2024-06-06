package com.ruoyi.web.controller.law.srv.incremental;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
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

import java.util.List;

/**
 * @author xiao.hu
 * @date 2024-05-16
 * @apiNote
 */
@Service
public class IncrementalDataSrv {

    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlAssociatedFileService slAssociatedFileService;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    /**
     * 如果DataSourceType.SLAVE 没有配置，会自动切回访问主库
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
     * 获取增量法条数据
     * @param lawId
     * @return
     */
    @DataSource(DataSourceType.SLAVE)
    public List<SlLawProvision> getIncrementalLawProvision(long lawId) {
        SlLawProvision provision = new SlLawProvision();
        provision.setLawId(lawId);
        List<SlLawProvision> provisionList = slLawProvisionService.selectSlLawProvisionList(provision);
        for(SlLawProvision p : provisionList) {
            p.setId(null);
            p.setUpdateTime(null);
            p.setCreateTime(null);
            p.setLawId(null);
        }

        return provisionList;
    }

    /**
     * 获取增量法条数据
     * @param lawId
     * @return
     */
    @DataSource(DataSourceType.SLAVE)
    public List<SlAssociatedFile> getIncrementalAssociatedFile(long lawId) {
        SlAssociatedFile associatedFile = new SlAssociatedFile();
        associatedFile.setLawId(lawId);
        List<SlAssociatedFile> associatedFilesList = slAssociatedFileService.selectSlAssociatedFileList(associatedFile);
        for(SlAssociatedFile p : associatedFilesList) {
            p.setId(null);
            p.setUpdateTime(null);
            p.setCreateTime(null);
            p.setLawId(null);
        }

        return associatedFilesList;
    }
}
