package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.SlLawCategory;
import org.apache.ibatis.annotations.Param;

/**
 * 法库目录Mapper接口
 * 
 * @author ruoyi
 * @date 2023-12-23
 */
public interface SlLawCategoryMapper 
{

    /**
     * 统计总数
     * @return
     */
    int count();


    /**
     * 查询法库目录
     * 
     * @param id 法库目录主键
     * @return 法库目录
     */
    public SlLawCategory selectSlLawCategoryById(String id);


    /**
     * 用 id 集合一次性获取对应的目录
     * @param idList
     * @param columns
     * @return
     */
    List<SlLawCategory> getByIds(@Param("idList") List<Long> idList, @Param("columns") String[] columns);

    /**
     * 指定列的id查找
     * @param id
     * @param columns
     * @return
     */
    SlLawCategory getById(@Param("id") Long id, @Param("columns") String[] columns);

    /**
     * 查询法库目录列表
     * 
     * @param slLawCategory 法库目录
     * @return 法库目录集合
     */
    public List<SlLawCategory> selectSlLawCategoryList(SlLawCategory slLawCategory);

    /**
     * 列举
     * @param slLawCategory
     * @param columns
     * @return
     */
    List<SlLawCategory> listLawCategory(@Param("p") SlLawCategory slLawCategory, @Param("columns") String[] columns);

    /**
     * 列举效力
     * @return
     */
    List<String> listLawLevel();


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
     * 删除法库目录
     * 
     * @param id 法库目录主键
     * @return 结果
     */
    public int deleteSlLawCategoryById(String id);

    /**
     * 批量删除法库目录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSlLawCategoryByIds(String[] ids);
}
