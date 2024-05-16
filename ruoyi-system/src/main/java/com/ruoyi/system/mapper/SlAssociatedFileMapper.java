package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SlAssociatedFile;

/**
 * 关联文件Mapper接口
 * 
 * @author xiao.hu
 * @date 2024-01-04
 */
public interface SlAssociatedFileMapper 
{

    /**
     * 统计总数
     * @return
     */
    int count();

    /**
     * 统计总数
     * @return
     */
    int count(long lawId);

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
     * 删除关联文件
     * 
     * @param id 关联文件主键
     * @return 结果
     */
    public int deleteSlAssociatedFileById(String id);

    /**
     * 批量删除关联文件
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSlAssociatedFileByIds(String[] ids);
}
