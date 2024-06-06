package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SlIncrementalLogMapper;
import com.ruoyi.system.domain.SlIncrementalLog;
import com.ruoyi.system.service.ISlIncrementalLogService;

import javax.annotation.Resource;

/**
 * 增量更新日志Service业务层处理
 * 
 * @author xiao.hu
 * @date 2024-05-17
 */
@Service
public class SlIncrementalLogServiceImpl implements ISlIncrementalLogService 
{
    @Resource
    private SlIncrementalLogMapper slIncrementalLogMapper;

    /**
     * 查询增量更新日志
     * 
     * @param id 增量更新日志主键
     * @return 增量更新日志
     */
    @Override
    public SlIncrementalLog selectSlIncrementalLogById(String id)
    {
        return slIncrementalLogMapper.selectSlIncrementalLogById(id);
    }

    /**
     * 查询增量更新日志列表
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 增量更新日志
     */
    @Override
    public List<SlIncrementalLog> selectSlIncrementalLogList(SlIncrementalLog slIncrementalLog)
    {
        return slIncrementalLogMapper.selectSlIncrementalLogList(slIncrementalLog);
    }

    /**
     * 新增增量更新日志
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 结果
     */
    @Override
    public int insertSlIncrementalLog(SlIncrementalLog slIncrementalLog)
    {
        slIncrementalLog.setCreateTime(DateUtils.getNowDate());
        return slIncrementalLogMapper.insertSlIncrementalLog(slIncrementalLog);
    }

    /**
     * 修改增量更新日志
     * 
     * @param slIncrementalLog 增量更新日志
     * @return 结果
     */
    @Override
    public int updateSlIncrementalLog(SlIncrementalLog slIncrementalLog)
    {
        slIncrementalLog.setUpdateTime(DateUtils.getNowDate());
        return slIncrementalLogMapper.updateSlIncrementalLog(slIncrementalLog);
    }

    /**
     * 批量删除增量更新日志
     * 
     * @param ids 需要删除的增量更新日志主键
     * @return 结果
     */
    @Override
    public int deleteSlIncrementalLogByIds(String[] ids)
    {
        return slIncrementalLogMapper.deleteSlIncrementalLogByIds(ids);
    }

    /**
     * 删除增量更新日志信息
     * 
     * @param id 增量更新日志主键
     * @return 结果
     */
    @Override
    public int deleteSlIncrementalLogById(String id)
    {
        return slIncrementalLogMapper.deleteSlIncrementalLogById(id);
    }
}
