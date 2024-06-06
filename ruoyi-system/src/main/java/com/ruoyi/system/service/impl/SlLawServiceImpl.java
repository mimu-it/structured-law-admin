package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.mapper.SlLawMapper;
import com.ruoyi.system.service.ISlLawService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 法律信息Service业务层处理
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
@Service
public class SlLawServiceImpl implements ISlLawService
{
    @Resource
    private SlLawMapper slLawMapper;

    /**
     * 统计总数
     * @return
     */
    @Override
    public int count() {
        return slLawMapper.count();
    }


    @Override
    public List<SlLaw> listAuthority(String authorityProvince, String authorityCity) {
        return slLawMapper.listAuthority(authorityProvince, authorityCity);
    }

    @Override
    public List<SlLaw> listCity(String authorityProvince) {
        return slLawMapper.listCity(authorityProvince);
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
     *
     * @param idList
     * @param columns
     * @return
     */
    @Override
    public List<SlLaw> getByIds(List<Long> idList, String[] columns) {
        if(idList.isEmpty()) {
            return new ArrayList<>();
        }
        return slLawMapper.getByIds(idList, columns);
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
     *
     * @param slLaw
     * @param columns
     * @return
     */
    @Override
    public List<SlLaw> selectLawList(SlLaw slLaw, String[] columns) {
        return slLawMapper.selectLawList(slLaw, columns);
    }

    @Override
    public List<SlLaw> selectLawListForIncrementalUpdate(SlLaw slLaw, String[] columns) {
        return slLawMapper.selectLawListForIncrementalUpdate(slLaw, columns);
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
