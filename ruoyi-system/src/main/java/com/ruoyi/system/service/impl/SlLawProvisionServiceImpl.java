package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SlLawProvisionMapper;
import com.ruoyi.system.domain.SlLawProvision;
import com.ruoyi.system.service.ISlLawProvisionService;

import javax.annotation.Resource;

/**
 * 法律条款Service业务层处理
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
@Service
public class SlLawProvisionServiceImpl implements ISlLawProvisionService 
{
    @Resource
    private SlLawProvisionMapper slLawProvisionMapper;

    @Override
    public int count() {
        return slLawProvisionMapper.count();
    }

    /**
     * 统计有tags的总数
     * @return
     */
    @Override
    public int countTags() {
        return slLawProvisionMapper.countTags();
    }

    @Override
    public List<SlLawProvision> selectSlLawProvisionTagsList() {
        return slLawProvisionMapper.selectSlLawProvisionTagsList();
    }


    /**
     * 查询法律条款
     * 
     * @param id 法律条款主键
     * @return 法律条款
     */
    @Override
    public SlLawProvision selectSlLawProvisionById(Long id)
    {
        return slLawProvisionMapper.selectSlLawProvisionById(id);
    }

    /**
     * 查询法律条款列表
     * 
     * @param slLawProvision 法律条款
     * @return 法律条款
     */
    @Override
    public List<SlLawProvision> selectSlLawProvisionList(SlLawProvision slLawProvision)
    {
        return slLawProvisionMapper.selectSlLawProvisionList(slLawProvision);
    }

    /**
     * 新增法律条款
     * 
     * @param slLawProvision 法律条款
     * @return 结果
     */
    @Override
    public int insertSlLawProvision(SlLawProvision slLawProvision)
    {
        return slLawProvisionMapper.insertSlLawProvision(slLawProvision);
    }

    /**
     * 修改法律条款
     * 
     * @param slLawProvision 法律条款
     * @return 结果
     */
    @Override
    public int updateSlLawProvision(SlLawProvision slLawProvision)
    {
        return slLawProvisionMapper.updateSlLawProvision(slLawProvision);
    }

    /**
     * 批量删除法律条款
     * 
     * @param ids 需要删除的法律条款主键
     * @return 结果
     */
    @Override
    public int deleteSlLawProvisionByIds(Long[] ids)
    {
        return slLawProvisionMapper.deleteSlLawProvisionByIds(ids);
    }

    /**
     * 删除法律条款信息
     * 
     * @param id 法律条款主键
     * @return 结果
     */
    @Override
    public int deleteSlLawProvisionById(Long id)
    {
        return slLawProvisionMapper.deleteSlLawProvisionById(id);
    }
}
