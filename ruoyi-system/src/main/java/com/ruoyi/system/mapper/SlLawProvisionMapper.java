package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SlLawProvision;

/**
 * 法律条款Mapper接口
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
public interface SlLawProvisionMapper 
{
    /**
     * 统计总数
     * @return
     */
    int count();

    /**
     * 统计tags
     * @return
     */
    int countTags();

    /**
     * 获取有标签的Provision
     * @return
     */
    List<SlLawProvision> selectSlLawProvisionTagsList();

    /**
     * 查询法律条款
     * 
     * @param id 法律条款主键
     * @return 法律条款
     */
    public SlLawProvision selectSlLawProvisionById(Long id);

    /**
     * 查询法律条款列表
     * 
     * @param slLawProvision 法律条款
     * @return 法律条款集合
     */
    public List<SlLawProvision> selectSlLawProvisionList(SlLawProvision slLawProvision);

    /**
     * 新增法律条款
     * 
     * @param slLawProvision 法律条款
     * @return 结果
     */
    public int insertSlLawProvision(SlLawProvision slLawProvision);

    /**
     * 修改法律条款
     * 
     * @param slLawProvision 法律条款
     * @return 结果
     */
    public int updateSlLawProvision(SlLawProvision slLawProvision);

    /**
     * 删除法律条款
     * 
     * @param id 法律条款主键
     * @return 结果
     */
    public int deleteSlLawProvisionById(Long id);

    /**
     * 批量删除法律条款
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSlLawProvisionByIds(Long[] ids);
}
