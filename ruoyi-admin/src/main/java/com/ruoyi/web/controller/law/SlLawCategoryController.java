package com.ruoyi.web.controller.law;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.domain.BaseEntity;
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
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 法库目录Controller
 * 
 * @author ruoyi
 * @date 2023-12-23
 */
@RestController
@RequestMapping("/structured-law/category")
public class SlLawCategoryController extends BaseController
{
    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    /**
     * 查询法库目录列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(SlLawCategory slLawCategory)
    {
        startPage();
        List<SlLawCategory> list = slLawCategoryService.selectSlLawCategoryList(slLawCategory);
        return getDataTable(list);
    }


    /**
     * 查询法库目录列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:list-all')")
    @GetMapping("/list-all")
    public AjaxResult listAll(SlLawCategory slLawCategory) {
        List<SlLawCategory> list = slLawCategoryService.listLawCategory(slLawCategory, new String[]{
                BaseEntity.ID, SlLawCategory.FOLDER, SlLawCategory.CATEGORY_ORDER, SlLawCategory.NAME
        });
        return success(list);
    }


    /**
     * 导出法库目录列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:export')")
    @Log(title = "法库目录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlLawCategory slLawCategory)
    {
        List<SlLawCategory> list = slLawCategoryService.selectSlLawCategoryList(slLawCategory);
        ExcelUtil<SlLawCategory> util = new ExcelUtil<SlLawCategory>(SlLawCategory.class);
        util.exportExcel(response, list, "法库目录数据");
    }

    /**
     * 获取法库目录详细信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(slLawCategoryService.selectSlLawCategoryById(id));
    }

    /**
     * 新增法库目录
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:add')")
    @Log(title = "法库目录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SlLawCategory slLawCategory)
    {
        return toAjax(slLawCategoryService.insertSlLawCategory(slLawCategory));
    }

    /**
     * 修改法库目录
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:edit')")
    @Log(title = "法库目录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SlLawCategory slLawCategory)
    {
        return toAjax(slLawCategoryService.updateSlLawCategory(slLawCategory));
    }

    /**
     * 删除法库目录
     */
    @PreAuthorize("@ss.hasPermi('structured-law:category:remove')")
    @Log(title = "法库目录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(slLawCategoryService.deleteSlLawCategoryByIds(ids));
    }
}
