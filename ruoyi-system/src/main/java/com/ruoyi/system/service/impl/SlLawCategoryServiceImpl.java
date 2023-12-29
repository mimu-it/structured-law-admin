package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SlLawCategoryMapper;
import com.ruoyi.system.domain.SlLawCategory;
import com.ruoyi.system.service.ISlLawCategoryService;

import javax.annotation.Resource;

/**
 * 法库目录Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-12-23
 */
@Service
public class SlLawCategoryServiceImpl implements ISlLawCategoryService 
{
    @Resource
    private SlLawCategoryMapper slLawCategoryMapper;

    /**
     * 查询法库目录
     * 
     * @param id 法库目录主键
     * @return 法库目录
     */
    @Override
    public SlLawCategory selectSlLawCategoryById(String id)
    {
        return slLawCategoryMapper.selectSlLawCategoryById(id);
    }

    /**
     *
     * @param id
     * @param columns
     * @return
     */
    @Override
    public SlLawCategory getById(Long id, String[] columns) {
        return slLawCategoryMapper.getById(id, columns);
    }

    /**
     * 查询法库目录列表
     * 
     * @param slLawCategory 法库目录
     * @return 法库目录
     */
    @Override
    public List<SlLawCategory> selectSlLawCategoryList(SlLawCategory slLawCategory)
    {
        return slLawCategoryMapper.selectSlLawCategoryList(slLawCategory);
    }

    /**
     * 列举
     * @param slLawCategory 法库目录
     * @param columns
     * @return
     */
    @Override
    public List<SlLawCategory> listLawCategory(SlLawCategory slLawCategory, String[] columns) {
        return slLawCategoryMapper.listLawCategory(slLawCategory, columns);
    }

    /**
     *
     * @return
     */
    @Override
    public List<String> listLawType() {
        return slLawCategoryMapper.listLawType();
    }

    /**
     * 新增法库目录
     * 
     * @param slLawCategory 法库目录
     * @return 结果
     */
    @Override
    public int insertSlLawCategory(SlLawCategory slLawCategory)
    {
        return slLawCategoryMapper.insertSlLawCategory(slLawCategory);
    }

    /**
     * 修改法库目录
     * 
     * @param slLawCategory 法库目录
     * @return 结果
     */
    @Override
    public int updateSlLawCategory(SlLawCategory slLawCategory)
    {
        return slLawCategoryMapper.updateSlLawCategory(slLawCategory);
    }

    /**
     * 批量删除法库目录
     * 
     * @param ids 需要删除的法库目录主键
     * @return 结果
     */
    @Override
    public int deleteSlLawCategoryByIds(String[] ids)
    {
        return slLawCategoryMapper.deleteSlLawCategoryByIds(ids);
    }

    /**
     * 删除法库目录信息
     * 
     * @param id 法库目录主键
     * @return 结果
     */
    @Override
    public int deleteSlLawCategoryById(String id)
    {
        return slLawCategoryMapper.deleteSlLawCategoryById(id);
    }
}
