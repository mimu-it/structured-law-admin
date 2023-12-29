package com.ruoyi.system.service;

import java.util.List;

import com.ruoyi.system.domain.SlLawCategory;

/**
 * 法库目录Service接口
 * 
 * @author ruoyi
 * @date 2023-12-23
 */
public interface ISlLawCategoryService 
{
    /**
     * 查询法库目录
     * 
     * @param id 法库目录主键
     * @return 法库目录
     */
    public SlLawCategory selectSlLawCategoryById(String id);

    /**
     * 通过id 查指定列
     * @param id
     * @param columns
     * @return
     */
    SlLawCategory getById(Long id, String[] columns);

    /**
     * 查询法库目录列表
     * 
     * @param slLawCategory 法库目录
     * @return 法库目录集合
     */
    public List<SlLawCategory> selectSlLawCategoryList(SlLawCategory slLawCategory);


    /**
     * 列举法库目录
     *
     * @param slLawCategory 法库目录
     * @return 法库目录集合
     */
    List<SlLawCategory> listLawCategory(SlLawCategory slLawCategory, String[] columns);

    /**
     * 列举"效力"， 实际就是法规类型
     * @return
     */
    List<String> listLawType();

    /**
     * 新增法库目录
     * 
     * @param slLawCategory 法库目录
     * @return 结果
     */
    public int insertSlLawCategory(SlLawCategory slLawCategory);

    /**
     * 修改法库目录
     * 
     * @param slLawCategory 法库目录
     * @return 结果
     */
    public int updateSlLawCategory(SlLawCategory slLawCategory);

    /**
     * 批量删除法库目录
     * 
     * @param ids 需要删除的法库目录主键集合
     * @return 结果
     */
    public int deleteSlLawCategoryByIds(String[] ids);

    /**
     * 删除法库目录信息
     * 
     * @param id 法库目录主键
     * @return 结果
     */
    public int deleteSlLawCategoryById(String id);
}
