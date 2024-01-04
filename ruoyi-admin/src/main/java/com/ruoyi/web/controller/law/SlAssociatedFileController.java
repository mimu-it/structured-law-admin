package com.ruoyi.web.controller.law;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.system.domain.SlAssociatedFile;
import com.ruoyi.system.service.ISlAssociatedFileService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 关联文件Controller
 * 
 * @author xiao.hu
 * @date 2024-01-04
 */
@RestController
@RequestMapping("/structured-law/other-file")
public class SlAssociatedFileController extends BaseController
{
    @Autowired
    private ISlAssociatedFileService slAssociatedFileService;

    /**
     * 查询关联文件列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:list')")
    @GetMapping("/list")
    public TableDataInfo list(SlAssociatedFile slAssociatedFile)
    {
        startPage();
        List<SlAssociatedFile> list = slAssociatedFileService.selectSlAssociatedFileList(slAssociatedFile);
        return getDataTable(list);
    }

    /**
     * 导出关联文件列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:export')")
    @Log(title = "关联文件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlAssociatedFile slAssociatedFile)
    {
        List<SlAssociatedFile> list = slAssociatedFileService.selectSlAssociatedFileList(slAssociatedFile);
        ExcelUtil<SlAssociatedFile> util = new ExcelUtil<SlAssociatedFile>(SlAssociatedFile.class);
        util.exportExcel(response, list, "关联文件数据");
    }

    /**
     * 获取关联文件详细信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(slAssociatedFileService.selectSlAssociatedFileById(id));
    }

    /**
     * 新增关联文件
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:add')")
    @Log(title = "关联文件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SlAssociatedFile slAssociatedFile)
    {
        return toAjax(slAssociatedFileService.insertSlAssociatedFile(slAssociatedFile));
    }

    /**
     * 修改关联文件
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:edit')")
    @Log(title = "关联文件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SlAssociatedFile slAssociatedFile)
    {
        return toAjax(slAssociatedFileService.updateSlAssociatedFile(slAssociatedFile));
    }

    /**
     * 删除关联文件
     */
    @PreAuthorize("@ss.hasPermi('structured-law:other-file:remove')")
    @Log(title = "关联文件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(slAssociatedFileService.deleteSlAssociatedFileByIds(ids));
    }
}
