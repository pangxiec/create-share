package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.ExceptionLog;
import com.example.creation.xo.vo.ExceptionLogVO;
import com.example.creation.base.service.SuperService;

/**
 * 操作异常日志 服务类
 *
 * @author limbo
 * @date 2018-09-30
 */
public interface ExceptionLogService extends SuperService<ExceptionLog> {

    /**
     * 获取异常日志列表
     *
     * @param exceptionLogVO
     * @return
     */
    public IPage<ExceptionLog> getPageList(ExceptionLogVO exceptionLogVO);
}
