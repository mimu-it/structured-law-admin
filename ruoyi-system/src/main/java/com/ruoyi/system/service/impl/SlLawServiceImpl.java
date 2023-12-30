package com.ruoyi.system.service.impl;

import java.util.List;

import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.mapper.SlLawMapper;
import com.ruoyi.system.service.ISlLawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 法律信息Service业务层处理
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
@Service
public class SlLawServiceImpl implements ISlLawService
{
    @Autowired
    private SlLawMapper slLawMapper;


    @Override
    public List<String> listAuthority() {
        return slLawMapper.listAuthority();
    }

    @Override
    public List<Integer> listStatus() {
        return slLawMapper.listStatus();
    }

    /**
     * 查询法律信息
     * 
     * @param id 法律信息主键
     * @return 法律信息
     */
    @Override
    public SlLaw selectSlLawById(Long id)
    {
        return slLawMapper.selectSlLawById(id);
    }

    /**
     * 指定列的id查找
     * @param id
     * @param columns
     * @return
     */
    @Override
    public SlLaw getById(Long id, String[] columns) {
        return slLawMapper.getById(id, columns);
    }

    /**
     * 查询法律信息列表
     *
     * @return 法律信息
     */
    @Override
    public List<SlLaw> selectSlLawListForSearch(Long categoryId, String name, String[] publishDateStrArray, String[] validFromDateStrArray) {
        return slLawMapper.selectSlLawListForSearch(categoryId, name, publishDateStrArray, validFromDateStrArray);
    }

    /**
     * 查询法律信息列表
     *
     * @param slLaw 法律信息
     * @return 法律信息
     */
    @Override
    public List<SlLaw> selectSlLawList(SlLaw slLaw)
    {
        return slLawMapper.selectSlLawList(slLaw);
    }

    /**
     * 新增法律信息
     * 
     * @param slLaw 法律信息
     * @return 结果
     */
    @Override
    public int insertSlLaw(SlLaw slLaw)
    {
        return slLawMapper.insertSlLaw(slLaw);
    }

    /**
     * 修改法律信息
     * 
     * @param slLaw 法律信息
     * @return 结果
     */
    @Override
    public int updateSlLaw(SlLaw slLaw)
    {
        return slLawMapper.updateSlLaw(slLaw);
    }

    /**
     * 批量删除法律信息
     * 
     * @param ids 需要删除的法律信息主键
     * @return 结果
     */
    @Override
    public int deleteSlLawByIds(Long[] ids)
    {
        return slLawMapper.deleteSlLawByIds(ids);
    }

    /**
     * 删除法律信息信息
     * 
     * @param id 法律信息主键
     * @return 结果
     */
    @Override
    public int deleteSlLawById(Long id)
    {
        return slLawMapper.deleteSlLawById(id);
    }
}
