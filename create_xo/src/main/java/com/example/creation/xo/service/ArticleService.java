package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.creation.commons.entity.Article;
import com.example.creation.xo.vo.ArticleVO;
import com.example.creation.base.service.SuperService;

import java.util.List;
import java.util.Map;

/**
 * @author xmy
 * @date 2021/3/15 14:31
 */
public interface ArticleService extends SuperService<Article> {

    /**
     * 给文章列表设置标签
     *
     * @param list
     * @return
     */
    List<Article> setTagByArticleList(List<Article> list);

    /**
     * 给文章列表设置分类和标签
     *
     * @param list
     * @return
     */
    List<Article> setTagAndSortByArticleList(List<Article> list);

    /**
     * 给文章列表设置分类，标签，图片
     *
     * @param list
     * @return
     */
    List<Article> setTagAndSortAndPictureByBlogList(List<Article> list);

    /**
     * 给文章设置标签
     *
     * @param article
     * @return
     */
    Article setTagByArticle(Article article);

    /**
     * 给文章设置分类
     *
     * @param article
     * @return
     */
    Article setSortByArticle(Article article);

    /**
     * 通过推荐等级获取文章列表
     *
     * @param level
     * @return
     */
    List<Article> getBlogListByLevel(Integer level);

    /**
     * 通过推荐等级获取文章Page，是否排序
     *
     * @param page
     * @param level
     * @param useSort
     * @return
     */
    IPage<Article> getBlogPageByLevel(Page<Article> page, Integer level, Integer useSort);

    /**
     * 通过状态获取文章数量
     *
     * @param status
     * @return
     */
    Integer getArticleCount(Integer status);

    /**
     * 通过标签获取文章数目
     *
     * @return
     */
    List<Map<String, Object>> getArticleCountByTag();

    /**
     * 通过标签获取文章数目
     *
     * @return
     */
    List<Map<String, Object>> getArticleCountByBlogSort();

    /**
     * 获取一年内的文章创作数
     *
     * @return
     */
    Map<String, Object> getArticleContributeCount();

    /**
     * 通过uid获取文章内容
     *
     * @param uid
     * @return
     */
    Article getBlogByUid(String uid);

    /**
     * 根据BlogUid获取相关的文章
     *
     * @param blogUid
     * @return
     */
    List<Article> getSameBlogByBlogUid(String blogUid);

    /**
     * 获取点击量前top的文章列表
     *
     * @param top
     * @return
     */
    List<Article> getBlogListByTop(Integer top);

    /**
     * 获取文章列表
     *
     * @param articleVO
     * @return
     */
    IPage<Article> getPageList(ArticleVO articleVO);

    /**
     * 新增文章
     * @param articleVO
     * @return
     */
    String addArticle(ArticleVO articleVO);

    /**
     * 编辑文章
     *
     * @param articleVO
     * @return
     */
    String editArticle(ArticleVO articleVO);

    /**
     * 推荐文章排序调整
     *
     * @param articleVOList
     * @return
     */
    String editBatch(List<ArticleVO> articleVOList);

    /**
     * 删除文章
     *
     * @param articleVO
     * @return
     */
    String deleteArticle(ArticleVO articleVO);

    /**
     * 批量删除文章
     *
     * @param articleVoList
     * @return
     */
    String deleteBatchArticle(List<ArticleVO> articleVoList);

    /**
     * 删除和文章分类有关的Redis缓存
     */
    void deleteRedisByBlogSort();

    /**
     * 删除和文章标签有关的Redis缓存
     */
    void deleteRedisByBlogTag();

    /**
     * 删除和文章有关的Redis缓存
     */
    void deleteRedisByBlog();

    //========================create-web使用==========================

    /**
     * 通过推荐等级获取文章Page
     *
     * @param level       推荐级别
     * @param currentPage 当前页
     * @param useSort     是否使用排序字段
     * @return
     */
    IPage<Article> getBlogPageByLevel(Integer level, Long currentPage, Integer useSort);

    /**
     * 获取首页排行文章
     *
     * @return
     */
    IPage<Article> getHotBlog();


    /**
     * 获取最新的文章
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> getNewBlog(Long currentPage, Long pageSize);

    /**
     * mogu-search调用获取文章的接口[包含内容]
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> getBlogBySearch(Long currentPage, Long pageSize);

    /**
     * 按时间戳获取文章
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> getBlogByTime(Long currentPage, Long pageSize);

    /**
     * 通过文章Uid获取点赞数
     *
     * @param uid
     * @return
     */
    Integer getBlogPraiseCountByUid(String uid);

    /**
     * 通过UID给文章点赞
     *
     * @param uid
     * @return
     */
    String praiseBlogByUid(String uid);

    /**
     * 根据标签Uid获取相关的文章
     *
     * @param tagUid
     * @return
     */
    IPage<Article> getSameBlogByTagUid(String tagUid);

    /**
     * 通过文章分类UID获取文章列表
     *
     * @param blogSortUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> getListByBlogSortUid(String blogSortUid, Long currentPage, Long pageSize);

    /**
     * 通过关键字搜索文章列表
     *
     * @param keywords
     * @param currentPage
     * @param pageSize
     * @return
     */
    Map<String, Object> getBlogByKeyword(String keywords, Long currentPage, Long pageSize);

    /**
     * 通过标签搜索文章
     *
     * @param tagUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> searchBlogByTag(String tagUid, Long currentPage, Long pageSize);

    /**
     * 通过文章分类搜索文章
     *
     * @param blogSortUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> searchBlogByBlogSort(String blogSortUid, Long currentPage, Long pageSize);

    /**
     * 通过作者搜索文章
     *
     * @param author
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Article> searchBlogByAuthor(String author, Long currentPage, Long pageSize);

    /**
     * 获取文章的归档日期
     *
     * @return
     */
    String getBlogTimeSortList();

    /**
     * 通过月份获取日期
     *
     * @param monthDate
     * @return
     */
    String getArticleByMonth(String monthDate);
}
