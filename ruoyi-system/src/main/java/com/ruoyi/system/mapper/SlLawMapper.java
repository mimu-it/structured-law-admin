package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SlLaw;
import com.ruoyi.system.domain.SlLawCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 法律信息Mapper接口
 * 
 * @author xiao.hu
 * @date 2023-12-23
 */
public interface SlLawMapper 
{

    /**
     * 统计总数
     * @return
     */
    int count();

    /**
     * 用 id 集合一次性获取对应的目录
     * @param idList
     * @param columns
     * @return
     */
    List<SlLaw> getByIds(@Param("idList") List<Long> idList, @Param("columns") String[] columns);

    /**
     * 获取制定机关选项
     * @return
     */
    List<SlLaw> listAuthority(@Param("authorityProvince")String authorityProvince,
                              @Param("authorityCity")String authorityCity);

    /**
     * 获取law相关的城市
     * @return
     */
    List<SlLaw> listCity(@Param("authorityProvince")String authorityProvince);

    /**
     * 获取状态选项
     * @return
     */
    List<Integer> listStatus();

    /**
     * 查询法律信息
     * 
     * @param id 法律信息主键
     * @return 法律信息
     */
    public SlLaw selectSlLawById(Long id);

    /**
     * 指定列的id查找
     * @param id
     * @param columns
     * @return
     */
    SlLaw getById(@Param("id") Long id,  @Param("columns") String[] columns);


    List<SlLaw> selectSlLawListForSearch(@Param("categoryId")Long categoryId,
                                         @Param("name")String name,
                                         @Param("publish")String[] publishDateStrArray,
                                         @Param("validFrom")String[] validFromDateStrArray);

    /**
     * 查询法律信息列表
     * 
     * @param slLaw 法律信息
     * @return 法律信息集合
     */
    public List<SlLaw> selectSlLawList(SlLaw slLaw);


    /**
     *
     * @param slLaw
     * @param columns
     * @return
     */
    List<SlLaw> selectLawList( @Param("slLaw")  SlLaw slLaw,  @Param("columns") String[] columns);

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
     * 删除法律信息
     * 
     * @param id 法律信息主键
     * @return 结果
     */
    public int deleteSlLawById(Long id);

    /**
     * 批量删除法律信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSlLawByIds(Long[] ids);
}
