package com.ruoyi.web.controller.law;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.system.domain.SlIncrementalLog;
import com.ruoyi.system.service.ISlIncrementalLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 增量更新日志Controller
 * 
 * @author xiao.hu
 * @date 2024-05-17
 */
@RestController
@RequestMapping("/structured-law/log")
public class SlIncrementalLogController extends BaseController
{
    @Resource
    private ISlIncrementalLogService slIncrementalLogService;

    /**
     * 查询增量更新日志列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(SlIncrementalLog slIncrementalLog)
    {
        startPage();
        List<SlIncrementalLog> list = slIncrementalLogService.selectSlIncrementalLogList(slIncrementalLog);
        return getDataTable(list);
    }

    /**
     * 导出增量更新日志列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:export')")
    @Log(title = "增量更新日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlIncrementalLog slIncrementalLog)
    {
        List<SlIncrementalLog> list = slIncrementalLogService.selectSlIncrementalLogList(slIncrementalLog);
        ExcelUtil<SlIncrementalLog> util = new ExcelUtil<SlIncrementalLog>(SlIncrementalLog.class);
        util.exportExcel(response, list, "增量更新日志数据");
    }

    /**
     * 获取增量更新日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(slIncrementalLogService.selectSlIncrementalLogById(id));
    }

    /**
     * 新增增量更新日志
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:add')")
    @Log(title = "增量更新日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SlIncrementalLog slIncrementalLog)
    {
        return toAjax(slIncrementalLogService.insertSlIncrementalLog(slIncrementalLog));
    }

    /**
     * 修改增量更新日志
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:edit')")
    @Log(title = "增量更新日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SlIncrementalLog slIncrementalLog)
    {
        return toAjax(slIncrementalLogService.updateSlIncrementalLog(slIncrementalLog));
    }

    /**
     * 删除增量更新日志
     */
    @PreAuthorize("@ss.hasPermi('structured-law:log:remove')")
    @Log(title = "增量更新日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(slIncrementalLogService.deleteSlIncrementalLogByIds(ids));
    }
}
