package com.ruoyi.web.controller.law;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.system.service.ISlLawService;
import com.ruoyi.web.controller.law.srv.incremental.IncrementalDataSrv;
import com.ruoyi.web.controller.law.srv.incremental.IncrementalUpdateSrv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-05-16
 * @apiNote
 *
 * RequestMapping 需要配置sql
 *
 */
@RestController
@RequestMapping("/structured-law/incremental-update")
public class IncrementalUpdateController extends BaseController {

    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    @Autowired
    private IncrementalUpdateSrv incrementalUpdateSrv;

    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private IncrementalDataSrv incrementalDataSrv;

    /**
     * 合并增量数据到主库
     * @param lawIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:merge')")
    @PutMapping(value = {"/merge/{lawIds}", "/merge"})
    public AjaxResult merge(@PathVariable(value = "lawIds", required = false) List<Long> lawIds) {
        List<SlLaw> lawList = incrementalDataSrv.getIncrementalLaw(lawIds);
        incrementalUpdateSrv.merge(lawList);
        return toAjax(true);
    }


    /**
     * 查询法律信息列表
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(name= SlLaw.C__CATEGORY_ID, required=false) Long categoryId,
                              @RequestParam(name= SlLaw.NAME, required=false) String name,
                              @RequestParam(name= SlLaw.PUBLISH, required=false) String publishDateStr,
                              @RequestParam(name= SlLaw.C__VALID_FROM , required=false) String validFromDateStr) {
        startPage();

        String[] publishDateStrArray = null;
        if(StrUtil.isNotBlank(publishDateStr)) {
            publishDateStrArray = JSONUtil.toList(publishDateStr, String.class).toArray(new String[0]);
        }

        String[] validFromDateStrArray = null;
        if(StrUtil.isNotBlank(validFromDateStr)) {
            validFromDateStrArray = JSONUtil.toList(validFromDateStr, String.class).toArray(new String[0]);
        }

        Page<SlLaw> list = (Page<SlLaw>) slLawService.selectSlLawListForSearch(categoryId, name, publishDateStrArray, validFromDateStrArray);

        List<Long> categoryIdList = new ArrayList<>(list.size());
        list.forEach((law) -> categoryIdList.add(law.getCategoryId()));

        List<SlLawCategory> categoryNameHolderList = slLawCategoryService.getByIds(categoryIdList, new String[]{
                SlLaw.ID,
                SlLaw.NAME
        });

        Map<Long, String> categoryIdNameMap = new HashMap<>(categoryNameHolderList.size());
        categoryNameHolderList.forEach((lawCategory) -> categoryIdNameMap.put(lawCategory.getId(), lawCategory.getName()));

        List<Map<String, Object>> mapList = new ArrayList<>(list.size());
        for(SlLaw row : list) {
            Map<String, Object> mapItem = BeanUtil.beanToMap(row, false, true);
            if(ObjectUtil.isNotNull(row.getCategoryId())) {
                mapItem.put(Constants.ESCAPED_PREFIX + SlLaw.CATEGORY_ID, categoryIdNameMap.get(row.getCategoryId()));
            }

            mapList.add(mapItem);
        }

        Page<Map<String, Object>> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(mapList);

        return getDataTable(newPage);
    }


    /**
     * 查询增量更新法库目录列表
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:list-category')")
    @GetMapping("/list-category")
    public AjaxResult listAll(SlLawCategory slLawCategory) {
        List<SlLawCategory> list = slLawCategoryService.listLawCategory(slLawCategory, new String[]{
                BaseEntity.ID, SlLawCategory.FOLDER, SlLawCategory.CATEGORY_ORDER, SlLawCategory.NAME
        });
        return success(list);
    }


    /** ================== 法律 ====================*/

    /**
     * 获取法律信息详细信息
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(slLawService.selectSlLawById(id));
    }

    /**
     * 新增法律信息
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SlLaw slLaw)
    {
        return toAjax(slLawService.insertSlLaw(slLaw));
    }

    /**
     * 修改法律信息
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SlLaw slLaw)
    {
        return toAjax(slLawService.updateSlLaw(slLaw));
    }

    /**
     * 删除法律信息
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(incrementalUpdateSrv.removeLaw(ids));
    }


    /** ================== 法律条款 ====================*/

    /**
     * 查询法律条款列表
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:provision:list')")
    @GetMapping("/provision/list")
    public TableDataInfo list(SlLawProvision slLawProvision) {
        startPage();
        Page<SlLawProvision> list = (Page<SlLawProvision>) slLawProvisionService.selectSlLawProvisionList(slLawProvision);
        List<Map<String, Object>> mapList = new ArrayList<>(list.size());

        List<Long> lawIdList = new ArrayList<>(list.size());
        list.forEach((provision) -> lawIdList.add(provision.getLawId()));

        List<SlLaw> lawNameHolderList = slLawService.getByIds(lawIdList, new String[]{
                SlLaw.ID,
                SlLaw.NAME
        });

        Map<Long, String> lawIdNameMap = new HashMap<>(lawNameHolderList.size());
        lawNameHolderList.forEach((law) -> lawIdNameMap.put(law.getId(), law.getName()));

        for(SlLawProvision row : list) {
            Map<String, Object> mapItem = BeanUtil.beanToMap(row, false, true);
            if(ObjectUtil.isNotNull(row.getLawId())) {
                mapItem.put(Constants.ESCAPED_PREFIX + SlLawProvision.LAW_ID, lawIdNameMap.get(row.getLawId()));
            }

            mapList.add(mapItem);
        }

        Page<Map<String, Object>> newPage = new Page<>();
        BeanUtil.copyProperties(list, newPage);
        newPage.clear();
        newPage.addAll(mapList);

        return getDataTable(newPage);
    }

    /**
     * 获取法律条款详细信息
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:provision:query')")
    @GetMapping(value = "/provision/{id}")
    public AjaxResult getProvisionInfo(@PathVariable("id") Long id) {
        return success(slLawProvisionService.selectSlLawProvisionById(id));
    }

    /**
     * 新增法律条款
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:provision:add')")
    @PostMapping(value = "/provision")
    public AjaxResult addProvision(@RequestBody SlLawProvision slLawProvision) {
        return toAjax(slLawProvisionService.insertSlLawProvision(slLawProvision));
    }

    /**
     * 修改法律条款
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:provision:edit')")
    @PutMapping(value = "/provision")
    public AjaxResult editProvision(@RequestBody SlLawProvision slLawProvision) {
        return toAjax(slLawProvisionService.updateSlLawProvision(slLawProvision));
    }

    /**
     * 删除法律条款
     */
    @DataSource(DataSourceType.SLAVE)
    @PreAuthorize("@ss.hasPermi('structured-law:incremental_update:provision:remove')")
    @DeleteMapping("/provision/{ids}")
    public AjaxResult removeProvision(@PathVariable Long[] ids) {
        return toAjax(slLawProvisionService.deleteSlLawProvisionByIds(ids));
    }
}
