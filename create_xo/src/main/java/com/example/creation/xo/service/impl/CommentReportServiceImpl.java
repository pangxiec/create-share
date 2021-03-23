package com.example.creation.xo.service.impl;

import com.example.creation.commons.entity.CommentReport;
import com.example.creation.xo.mapper.CommentReportMapper;
import com.example.creation.xo.service.CommentReportService;
import com.example.creation.base.serviceImpl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 评论举报表 服务实现类
 *
 */
@Service
public class CommentReportServiceImpl extends SuperServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {

}
