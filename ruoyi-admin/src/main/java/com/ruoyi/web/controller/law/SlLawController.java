package com.ruoyi.web.controller.law;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.TempCachedCall;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.service.ISlLawCategoryService;
import com.ruoyi.system.service.ISlLawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 法律信息Controller
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
@RestController
@RequestMapping("/structured-law/law")
public class SlLawController extends BaseController
{
    @Autowired
    private ISlLawService slLawService;

    @Autowired
    private ISlLawCategoryService slLawCategoryService;

    /**
     * 查询法律信息列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:list')")
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

        List<Map<String, Object>> mapList = new ArrayList<>(list.size());

        TempCachedCall<Long, String> tmpCacheCall = new TempCachedCall();
        for(SlLaw row : list) {
            Map<String, Object> mapItem = BeanUtil.beanToMap(row, false, true);
            if(ObjectUtil.isNotNull(row.getCategoryId())) {
                String valueTransferred = tmpCacheCall.call(row.getCategoryId(), (param) -> {

                    SlLawCategory category = slLawCategoryService.getById(row.getCategoryId(), new String[]{
                            SlLaw.NAME
                    });

                    return category.getName();
                });

                mapItem.put(Constants.ESCAPED_PREFIX + SlLaw.CATEGORY_ID, valueTransferred);
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
     * 数据量过大会超时
     * 导出法律信息列表
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:export')")
    @Log(title = "法律信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SlLaw slLaw)
    {
        List<SlLaw> list = slLawService.selectSlLawList(slLaw);
        ExcelUtil<SlLaw> util = new ExcelUtil<SlLaw>(SlLaw.class);
        util.exportExcel(response, list, "法律信息数据");
    }

    /**
     *
     * @param response
     * @param slLaw
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:export_existing_law')")
    @Log(title = "法律信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export-existing-law")
    public void exportExistingLaw(HttpServletResponse response, SlLaw slLaw) {
        List<SlLaw> list = slLawService.selectLawList(slLaw, new String[]{
                SlLaw.NAME,
                SlLaw.AUTHORITY,
                SlLaw.LAW_LEVEL,
                SlLaw.PUBLISH,
                SlLaw.STATUS
        });

        Map<String, String> hashDict = new HashMap<>(list.size());
        for(SlLaw law : list) {
            Date publish = law.getPublish();
            String publishStr = DateUtil.format(publish, "yyyy-MM-dd HH:mm:ss");
            publishStr = Optional.ofNullable(publishStr).orElse("");

            String name = Optional.ofNullable(law.getName()).orElse("");
            String lawLevel = Optional.ofNullable(law.getLawLevel()).orElse("");
            String authority = Optional.ofNullable(law.getAuthority()).orElse("");
            String status = law.getStatus() == null ? "" : Integer.toString(law.getStatus());

            String key = StrUtil.join(",", name, authority, lawLevel, publishStr, status);
            hashDict.put(key, status);
        }

        // 创建JSONConfig，指定日期格式
        JSONConfig config = new JSONConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");

        // 使用JSONUtil.toJSONString方法，传入配置
        String jsonStr = JSONUtil.toJsonStr(hashDict, config);
        byte[] bytes = jsonStr.getBytes();

        response.setHeader("Content-Disposition","attachement; fileName=existing_law.json");
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(bytes);
        } catch (IOException e) {
            logger.error("", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取法律信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(slLawService.selectSlLawById(id));
    }

    /**
     * 新增法律信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:add')")
    @Log(title = "法律信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SlLaw slLaw)
    {
        return toAjax(slLawService.insertSlLaw(slLaw));
    }

    /**
     * 修改法律信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:edit')")
    @Log(title = "法律信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SlLaw slLaw)
    {
        return toAjax(slLawService.updateSlLaw(slLaw));
    }

    /**
     * 删除法律信息
     */
    @PreAuthorize("@ss.hasPermi('structured-law:law:remove')")
    @Log(title = "法律信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(slLawService.deleteSlLawByIds(ids));
    }
}
