package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SlAssociatedFileMapper;
import com.ruoyi.system.domain.SlAssociatedFile;
import com.ruoyi.system.service.ISlAssociatedFileService;

import javax.annotation.Resource;

/**
 * 关联文件Service业务层处理
 * 
 * @author xiao.hu
 * @date 2024-01-04
 */
@Service
public class SlAssociatedFileServiceImpl implements ISlAssociatedFileService 
{
    @Resource
    private SlAssociatedFileMapper slAssociatedFileMapper;

    @Override
    public int count() {
        return slAssociatedFileMapper.count();
    }

    /**
     * 查询关联文件
     * 
     * @param id 关联文件主键
     * @return 关联文件
     */
    @Override
    public SlAssociatedFile selectSlAssociatedFileById(String id)
    {
        return slAssociatedFileMapper.selectSlAssociatedFileById(id);
    }

    /**
     * 查询关联文件列表
     * 
     * @param slAssociatedFile 关联文件
     * @return 关联文件
     */
    @Override
    public List<SlAssociatedFile> selectSlAssociatedFileList(SlAssociatedFile slAssociatedFile)
    {
        return slAssociatedFileMapper.selectSlAssociatedFileList(slAssociatedFile);
    }

    /**
     * 新增关联文件
     * 
     * @param slAssociatedFile 关联文件
     * @return 结果
     */
    @Override
    public int insertSlAssociatedFile(SlAssociatedFile slAssociatedFile)
    {
        slAssociatedFile.setCreateTime(DateUtils.getNowDate());
        return slAssociatedFileMapper.insertSlAssociatedFile(slAssociatedFile);
    }

    /**
     * 修改关联文件
     * 
     * @param slAssociatedFile 关联文件
     * @return 结果
     */
    @Override
    public int updateSlAssociatedFile(SlAssociatedFile slAssociatedFile)
    {
        slAssociatedFile.setUpdateTime(DateUtils.getNowDate());
        return slAssociatedFileMapper.updateSlAssociatedFile(slAssociatedFile);
    }

    /**
     * 批量删除关联文件
     * 
     * @param ids 需要删除的关联文件主键
     * @return 结果
     */
    @Override
    public int deleteSlAssociatedFileByIds(String[] ids)
    {
        return slAssociatedFileMapper.deleteSlAssociatedFileByIds(ids);
    }

    /**
     * 删除关联文件信息
     * 
     * @param id 关联文件主键
     * @return 结果
     */
    @Override
    public int deleteSlAssociatedFileById(String id)
    {
        return slAssociatedFileMapper.deleteSlAssociatedFileById(id);
    }
}
