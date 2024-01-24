package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SlAssociatedFile;

/**
 * 关联文件Service接口
 * 
 * @author xiao.hu
 * @date 2024-01-04
 */
public interface ISlAssociatedFileService 
{

    /**
     * 统计总数
     * @return
     */
    int count();

    /**
     * 查询关联文件
     * 
     * @param id 关联文件主键
     * @return 关联文件
     */
    public SlAssociatedFile selectSlAssociatedFileById(String id);

    /**
     * 查询关联文件列表
     * 
     * @param slAssociatedFile 关联文件
     * @return 关联文件集合
     */
    public List<SlAssociatedFile> selectSlAssociatedFileList(SlAssociatedFile slAssociatedFile);

    /**
     * 新增关联文件
     * 
     * @param slAssociatedFile 关联文件
     * @return 结果
     */
    public int insertSlAssociatedFile(SlAssociatedFile slAssociatedFile);

    /**
     * 修改关联文件
     * 
     * @param slAssociatedFile 关联文件
     * @return 结果
     */
    public int updateSlAssociatedFile(SlAssociatedFile slAssociatedFile);

    /**
     * 批量删除关联文件
     * 
     * @param ids 需要删除的关联文件主键集合
     * @return 结果
     */
    public int deleteSlAssociatedFileByIds(String[] ids);

    /**
     * 删除关联文件信息
     * 
     * @param id 关联文件主键
     * @return 结果
     */
    public int deleteSlAssociatedFileById(String id);
}
