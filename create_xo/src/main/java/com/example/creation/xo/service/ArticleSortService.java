package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.ArticleSort;
import com.example.creation.xo.vo.ArticleSortVO;
import com.example.creation.base.service.SuperService;

import java.util.List;

/**
 * @author xmy
 * @date 2021/3/17 11:43
 */
public interface ArticleSortService extends SuperService<ArticleSort> {
    /**
     * 获取文章分类列表
     *
     * @param articleSortVO
     * @return
     */
    IPage<ArticleSort> getPageList(ArticleSortVO articleSortVO);

    /**
     * 获取文章分类列表
     *
     * @return
     */
    List<ArticleSort> getList();

    /**
     * 新增文章分类
     *
     * @param articleSortVO
     * @return
     */
    String addArticleSort(ArticleSortVO articleSortVO);

    /**
     * 编辑文章分类
     * @param articleSortVO
     * @return
     */
    String editBlogSort(ArticleSortVO articleSortVO);

    /**
     * 批量删除文章分类
     *
     * @param articleSortVoList
     * @return
     */
    String deleteBatchBlogSort(List<ArticleSortVO> articleSortVoList);

    /**
     * 置顶文章分类
     *
     * @param articleSortVO
     * @return
     */
    String stickBlogSort(ArticleSortVO articleSortVO);

    /**
     * 通过点击量排序文章
     *
     * @return
     */
    String blogSortByClickCount();

    /**
     * 通过引用量排序文章
     *
     * @return
     */
    String blogSortByCite();

    /**
     * 获取排序最高的一个文章分类
     *
     * @return
     */
    ArticleSort getTopOne();
}
