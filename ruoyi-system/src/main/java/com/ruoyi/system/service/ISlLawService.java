package com.ruoyi.system.service;

import com.ruoyi.system.domain.SlLaw;

import java.util.List;

/**
 * 法律信息Service接口
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
public interface ISlLawService 
{
    /**
     * 查询法律信息
     * 
     * @param id 法律信息主键
     * @return 法律信息
     */
    public SlLaw selectSlLawById(Long id);


    SlLaw getById(Long id, String[] columns);

    /**
     * 查询法律信息列表
     *
     * @return 法律信息集合
     */
    public List<SlLaw> selectSlLawListForSearch(Long categoryId, String name, String[] publishDateStrArray, String[] validFromDateStrArray);


    public List<SlLaw> selectSlLawList(SlLaw slLaw);

    /**
     * 新增法律信息
     * 
     * @param slLaw 法律信息
     * @return 结果
     */
    public int insertSlLaw(SlLaw slLaw);

    /**
     * 修改法律信息
     * 
     * @param slLaw 法律信息
     * @return 结果
     */
    public int updateSlLaw(SlLaw slLaw);

    /**
     * 批量删除法律信息
     * 
     * @param ids 需要删除的法律信息主键集合
     * @return 结果
     */
    public int deleteSlLawByIds(Long[] ids);

    /**
     * 删除法律信息信息
     * 
     * @param id 法律信息主键
     * @return 结果
     */
    public int deleteSlLawById(Long id);
}
