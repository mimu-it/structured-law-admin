package com.ruoyi.web.controller.law.srv;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ruoyi.system.domain.SlAssociatedFile;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlAssociatedFileService;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.elasticsearch.domain.IntegralFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiao.hu
 * @date 2023-12-26
 * @apiNote
 */
@Service
public class PortalSrv {


    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlAssociatedFileService slAssociatedFileService;


    /**
     * 分页列出法律信息数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<IntegralFields> listLawByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize, null);

        SlLaw slLaw = new SlLaw();
        List<SlLaw> list = slLawService.selectSlLawList(slLaw);
        List<IntegralFields> integralFieldsList = new ArrayList<>(list.size());
        for(SlLaw law : list) {
            IntegralFields integralFields = new IntegralFields();
            /** set_id 用于为es指定_id, 这样插入同样的数据只会修改，不会新增 */
            integralFields.setEsDocId(String.valueOf(law.getId()));
            this.copyToIntegralFields(law, integralFields);
            integralFieldsList.add(integralFields);
        }

        Page<IntegralFields> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(integralFieldsList);

        return newPage;
    }

    /**
     * 分页列出关联文件信息数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<IntegralFields> listAssociatedFileByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize, null);

        SlAssociatedFile associatedFileParam = new SlAssociatedFile();
        List<SlAssociatedFile> list = slAssociatedFileService.selectSlAssociatedFileList(associatedFileParam);

        List<IntegralFields> integralFieldsList = new ArrayList<>(list.size());
        for(SlAssociatedFile associatedFile : list) {
            IntegralFields integralFields = new IntegralFields();
            /** set_id 用于为es指定_id, 这样插入同样的数据只会修改，不会新增 */
            integralFields.setEsDocId(String.valueOf(associatedFile.getId()));
            integralFields.setAssociatedFileId(associatedFile.getId());
            integralFields.setAssociatedFileName(associatedFile.getName());
            integralFields.setDocumentType(associatedFile.getDocumentType());
            integralFields.setLawId(associatedFile.getLawId());
            integralFields.setContentText(associatedFile.getContent());

            integralFieldsList.add(integralFields);
        }

        Page<IntegralFields> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(integralFieldsList);
        return newPage;
    }


    /**
     * law 把数据给 integralFields
     * SELECT * FROM structured_law.sl_law where DATE_FORMAT(publish, '%Y-%m-%d')="0000-00-00";
     * update structured_law.sl_law set publish = null where DATE_FORMAT(publish, '%Y-%m-%d')="0000-00-00";
     * update structured_law.sl_law set valid_from=null where DATE_FORMAT(valid_from, '%Y-%m-%d')="0000-00-00";
     * @param law
     * @param integralFields
     */
    private void copyToIntegralFields(SlLaw law, IntegralFields integralFields) {
        if(integralFields.getLawId() == null) {
            integralFields.setLawId(law.getId());
        }

        integralFields.setLawName(law.getName());
        integralFields.setLawLevel(law.getLawLevel());
        integralFields.setAuthority(law.getAuthority());
        integralFields.setAuthorityProvince(law.getAuthorityProvince());
        integralFields.setAuthorityCity(law.getAuthorityCity());
        integralFields.setAuthorityDistrict(law.getAuthorityDistrict());
        integralFields.setPublish(law.getPublish());
        integralFields.setStatus(law.getStatus());
        integralFields.setValidFrom(law.getValidFrom());
        integralFields.setTags(law.getTags());
        integralFields.setDocumentNo(law.getDocumentNo());
    }

    /**
     * 分页列出法律条款信息数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<IntegralFields> listProvisionByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize, null);

        SlLawProvision slLawProvision = new SlLawProvision();
        Page<SlLawProvision> list = (Page<SlLawProvision>) slLawProvisionService.selectSlLawProvisionList(slLawProvision);

        List<IntegralFields> integralFieldsList = new ArrayList<>(list.size());

        LFUCache<Long, SlLaw> cacheLaw = CacheUtil.newLFUCache(10);
        for(SlLawProvision row : list) {
            IntegralFields integralFields = new IntegralFields();
            /** set_id 用于为es指定_id, 这样插入同样的数据只会修改，不会新增 */
            integralFields.setEsDocId(String.valueOf(row.getId()));

            integralFields.setLawId(row.getLawId());
            integralFields.setProvisionId(row.getId());
            integralFields.setTitle(row.getTitle());
            integralFields.setTitleNumber(row.getTitleNumber());
            integralFields.setTermText(row.getTermText());

            if(ObjectUtil.isNotNull(row.getLawId())) {
                SlLaw law = cacheLaw.get(row.getLawId());
                if(law == null) {
                    law = slLawService.getById(row.getLawId(), new String[]{
                            SlLaw.ID,
                            SlLaw.CATEGORY_ID,
                            SlLaw.NAME,
                            SlLaw.LAW_LEVEL,
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

                this.copyToIntegralFields(law, integralFields);
                integralFields.setPreface(law.getPreface());
            }

            integralFieldsList.add(integralFields);
        }

        Page<IntegralFields> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(integralFieldsList);

        return newPage;
    }
}
