package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.SysParams;
import com.example.creation.xo.vo.SysParamsVO;
import com.example.creation.base.service.SuperService;

import java.util.List;

/**
 * 参数配置 服务类
 *
 * @author xmy
 * @date 2021/3/15 17:12
 */
public interface SysParamsService extends SuperService<SysParams> {

    /**
     * 获取参数配置列表
     *
     * @param sysParamsVO
     * @return
     */
    IPage<SysParams> getPageList(SysParamsVO sysParamsVO);

    /**
     * 通过 参数键名 获取参数配置
     *
     * @param paramsKey
     * @return
     */
    SysParams getSysParamsByKey(String paramsKey);

    /**
     * 通过 参数键名 获取参数值
     *
     * @param paramsKey
     * @return
     */
    String getSysParamsValueByKey(String paramsKey);

    /**
     * 新增参数配置
     *
     * @param sysParamsVO
     */
    public String addSysParams(SysParamsVO sysParamsVO);

    /**
     * 编辑参数配置
     *
     * @param sysParamsVO
     */
    public String editSysParams(SysParamsVO sysParamsVO);

    /**
     * 批量删除参数配置
     *
     * @param sysParamsVOList
     */
    public String deleteBatchSysParams(List<SysParamsVO> sysParamsVOList);
}
