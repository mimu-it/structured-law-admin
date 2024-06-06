package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SlIncrementalLog;

/**
 * 增量更新日志Service接口
 * 
 * @author xiao.hu
 * @date 2024-05-17
 */
public interface ISlIncrementalLogService 
{
    /**
     * 查询增量更新日志
     * 
     * @param id 增量更新日志主键
     * @return 增量更新日志
     */
    public SlIncrementalLog selectSlIncrementalLogById(String id);

    /**
     * 查询增量更新日志列表
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 增量更新日志集合
     */
    public List<SlIncrementalLog> selectSlIncrementalLogList(SlIncrementalLog slIncrementalLog);

    /**
     * 新增增量更新日志
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 结果
     */
    public int insertSlIncrementalLog(SlIncrementalLog slIncrementalLog);

    /**
     * 修改增量更新日志
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 结果
     */
    public int updateSlIncrementalLog(SlIncrementalLog slIncrementalLog);

    /**
     * 批量删除增量更新日志
     * 
     * @param ids 需要删除的增量更新日志主键集合
     * @return 结果
     */
    public int deleteSlIncrementalLogByIds(String[] ids);

    /**
     * 删除增量更新日志信息
     * 
     * @param id 增量更新日志主键
     * @return 结果
     */
    public int deleteSlIncrementalLogById(String id);
}
