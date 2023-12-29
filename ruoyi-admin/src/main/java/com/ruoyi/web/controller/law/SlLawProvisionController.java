package com.ruoyi.web.controller.law;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.TempCachedCall;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.service.ISlLawService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlLawProvisionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 法律条款Controller
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
@RestController
@RequestMapping("/structured-law/provision")
public class SlLawProvisionController extends BaseController
{
    @Autowired
    private ISlLawProvisionService slLawProvisionService;

    @Autowired
    private ISlLawService slLawService;

    /**
     * 查询法律条款列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:list')")
    @GetMapping("/list")
    public TableDataInfo list(SlLawProvision slLawProvision)
    {
        startPage();
        Page<SlLawProvision> list = (Page<SlLawProvision>) slLawProvisionService.selectSlLawProvisionList(slLawProvision);
        List<Map<String, Object>> mapList = new ArrayList<>(list.size());

        TempCachedCall<Long, String> tmpCacheCall = new TempCachedCall();
        for(SlLawProvision row : list) {
            Map<String, Object> mapItem = BeanUtil.beanToMap(row, false, true);
            if(ObjectUtil.isNotNull(row.getLawId())) {
                String valueTransferred = tmpCacheCall.call(row.getLawId(), (param) -> {

                    SlLaw law = slLawService.getById(row.getLawId(), new String[]{
                            SlLaw.NAME
                    });

                    return law.getName();
                });

                mapItem.put(Constants.ESCAPED_PREFIX + SlLawProvision.LAW_ID, valueTransferred);
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
     * 导出法律条款列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:export')")
    @Log(title = "法律条款", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlLawProvision slLawProvision)
    {
        List<SlLawProvision> list = slLawProvisionService.selectSlLawProvisionList(slLawProvision);
        ExcelUtil<SlLawProvision> util = new ExcelUtil<SlLawProvision>(SlLawProvision.class);
        util.exportExcel(response, list, "法律条款数据");
    }

    /**
     * 获取法律条款详细信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(slLawProvisionService.selectSlLawProvisionById(id));
    }

    /**
     * 新增法律条款
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:add')")
    @Log(title = "法律条款", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SlLawProvision slLawProvision)
    {
        return toAjax(slLawProvisionService.insertSlLawProvision(slLawProvision));
    }

    /**
     * 修改法律条款
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:edit')")
    @Log(title = "法律条款", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SlLawProvision slLawProvision)
    {
        return toAjax(slLawProvisionService.updateSlLawProvision(slLawProvision));
    }

    /**
     * 删除法律条款
     */
    @PreAuthorize("@ss.hasPermi('structured-law:provision:remove')")
    @Log(title = "法律条款", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(slLawProvisionService.deleteSlLawProvisionByIds(ids));
    }
}
