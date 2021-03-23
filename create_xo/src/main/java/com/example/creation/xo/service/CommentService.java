package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.Comment;
import com.example.creation.xo.vo.CommentVO;
import com.example.creation.base.service.SuperService;

import java.util.List;

/**
 * 评论表 服务类
 *
 */
public interface CommentService extends SuperService<Comment> {

    /**
     * 获取评论数目
     *
     * @author xzx19950624@qq.com
     * @date 2018年10月22日下午3:43:38
     */
    public Integer getCommentCount(int status);

    /**
     * 获取评论列表
     *
     * @param commentVO
     * @return
     */
    public IPage<Comment> getPageList(CommentVO commentVO);

    /**
     * 新增评论
     *
     * @param commentVO
     */
    public String addComment(CommentVO commentVO);

    /**
     * 编辑评论
     *
     * @param commentVO
     */
    public String editComment(CommentVO commentVO);

    /**
     * 删除评论
     *
     * @param commentVO
     */
    public String deleteComment(CommentVO commentVO);

    /**
     * 批量删除评论
     *
     * @param commentVOList
     */
    public String deleteBatchComment(List<CommentVO> commentVOList);


}
