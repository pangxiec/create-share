package com.example.creation.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.creation.commons.entity.Article;
import com.example.creation.commons.entity.ArticleSort;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.xo.global.MessageConf;
import com.example.creation.xo.global.SQLConf;
import com.example.creation.xo.global.SysConf;
import com.example.creation.xo.mapper.BlogSortMapper;
import com.example.creation.xo.service.ArticleService;
import com.example.creation.xo.service.ArticleSortService;
import com.example.creation.xo.vo.ArticleSortVO;
import com.example.creation.base.enums.EPublish;
import com.example.creation.base.enums.EStatus;
import com.example.creation.base.serviceImpl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 文章分类表 服务实现类
 *
 *
 * @since 2018-09-08
 */
@Service
public class ArticleSortServiceImpl extends SuperServiceImpl<BlogSortMapper, ArticleSort> implements ArticleSortService {

    @Resource
    private ArticleSortService articleSortService;

    @Resource
    private ArticleService articleService;

    @Override
    public IPage<ArticleSort> getPageList(ArticleSortVO articleSortVO) {
        QueryWrapper<ArticleSort> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(articleSortVO.getKeyword()) && !StringUtils.isEmpty(articleSortVO.getKeyword().trim())) {
            queryWrapper.like(SQLConf.SORT_NAME, articleSortVO.getKeyword().trim());
        }
        if(StringUtils.isNotEmpty(articleSortVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(articleSortVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        }else if(StringUtils.isNotEmpty(articleSortVO.getOrderByDescColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(articleSortVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            queryWrapper.orderByDesc(SQLConf.SORT);
        }
        Page<ArticleSort> page = new Page<>();
        page.setCurrent(articleSortVO.getCurrentPage());
        page.setSize(articleSortVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        IPage<ArticleSort> pageList = articleSortService.page(page, queryWrapper);
        return pageList;
    }

    @Override
    public List<ArticleSort> getList() {
        QueryWrapper<ArticleSort> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SysConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.SORT);
        List<ArticleSort> articleSortList = articleSortService.list(queryWrapper);
        return articleSortList;
    }

    @Override
    public String addArticleSort(ArticleSortVO articleSortVO) {
        ArticleSort tempSort = articleSortService.getOne(new QueryWrapper<ArticleSort>()
                                                  .eq(SQLConf.SORT_NAME, articleSortVO.getSortName())
                                                  .eq(SQLConf.STATUS, EStatus.ENABLE));
        if (tempSort != null) {
            return ResultUtil.errorWithMessage(MessageConf.ENTITY_EXIST);
        }
        ArticleSort articleSort = new ArticleSort();
        articleSort.setContent(articleSortVO.getContent());
        articleSort.setSortName(articleSortVO.getSortName());
        articleSort.setSort(articleSortVO.getSort());
        articleSort.setStatus(EStatus.ENABLE);
        articleSort.insert();
        return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS);
    }

    @Override
    public String editBlogSort(ArticleSortVO articleSortVO) {
        ArticleSort articleSort = articleSortService.getById(articleSortVO.getUid());
        /**
         * 判断需要编辑的文章分类是否存在
         */
        if (!articleSort.getSortName().equals(articleSortVO.getSortName())) {
            QueryWrapper<ArticleSort> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SQLConf.SORT_NAME, articleSortVO.getSortName());
            queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
            ArticleSort tempSort = articleSortService.getOne(queryWrapper);
            if (tempSort != null) {
                return ResultUtil.errorWithMessage(MessageConf.ENTITY_EXIST);
            }
        }
        articleSort.setContent(articleSortVO.getContent());
        articleSort.setSortName(articleSortVO.getSortName());
        articleSort.setSort(articleSortVO.getSort());
        articleSort.setStatus(EStatus.ENABLE);
        articleSort.setUpdateTime(new Date());
        articleSort.updateById();
        // 删除和文章相关的Redis缓存
        articleService.deleteRedisByBlogSort();
        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    @Override
    public String deleteBatchBlogSort(List<ArticleSortVO> articleSortVoList) {
        if (articleSortVoList.size() <= 0) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();

        articleSortVoList.forEach(item -> {
            uids.add(item.getUid());
        });

        // 判断要删除的分类，是否有文章
        QueryWrapper<Article> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        blogQueryWrapper.in(SQLConf.BLOG_SORT_UID, uids);
        Integer blogCount = articleService.count(blogQueryWrapper);
        if (blogCount > 0) {
            return ResultUtil.errorWithMessage(MessageConf.BLOG_UNDER_THIS_SORT);
        }
        Collection<ArticleSort> articleSortList = articleSortService.listByIds(uids);
        articleSortList.forEach(item -> {
            item.setUpdateTime(new Date());
            item.setStatus(EStatus.DISABLED);
        });
        Boolean save = articleSortService.updateBatchById(articleSortList);
        if (save) {
            // 删除和文章相关的Redis缓存
            articleService.deleteRedisByBlogSort();
            return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
        } else {
            return ResultUtil.errorWithMessage(MessageConf.DELETE_FAIL);
        }
    }

    @Override
    public String stickBlogSort(ArticleSortVO articleSortVO) {
        ArticleSort articleSort = articleSortService.getById(articleSortVO.getUid());

        //查找出最大的那一个
        QueryWrapper<ArticleSort> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConf.SORT);
        Page<ArticleSort> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<ArticleSort> pageList = articleSortService.page(page, queryWrapper);
        List<ArticleSort> list = pageList.getRecords();
        ArticleSort maxSort = list.get(0);

        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        if (maxSort.getUid().equals(articleSort.getUid())) {
            return ResultUtil.errorWithMessage(MessageConf.THIS_SORT_IS_TOP);
        }
        Integer sortCount = maxSort.getSort() + 1;
        articleSort.setSort(sortCount);
        articleSort.setUpdateTime(new Date());
        articleSort.updateById();
        return ResultUtil.successWithMessage(MessageConf.OPERATION_SUCCESS);
    }

    @Override
    public String blogSortByClickCount() {
        QueryWrapper<ArticleSort> queryWrapper = new QueryWrapper();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        // 按点击从高到低排序
        queryWrapper.orderByDesc(SQLConf.CLICK_COUNT);
        List<ArticleSort> articleSortList = articleSortService.list(queryWrapper);
        // 设置初始化最大的sort值
        Integer maxSort = articleSortList.size();
        for (ArticleSort item : articleSortList) {
            item.setSort(item.getClickCount());
            item.setUpdateTime(new Date());
        }
        articleSortService.updateBatchById(articleSortList);
        return ResultUtil.successWithMessage(MessageConf.OPERATION_SUCCESS);
    }

    @Override
    public String blogSortByCite() {
        // 定义Map   key：tagUid,  value: 引用量
        Map<String, Integer> map = new HashMap<>();
        QueryWrapper<ArticleSort> blogSortQueryWrapper = new QueryWrapper<>();
        blogSortQueryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        List<ArticleSort> articleSortList = articleSortService.list(blogSortQueryWrapper);
        // 初始化所有标签的引用量
        articleSortList.forEach(item -> {
            map.put(item.getUid(), 0);
        });
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.IS_PUBLISH, EPublish.PUBLISH);
        // 过滤content字段
        queryWrapper.select(Article.class, i -> !i.getProperty().equals(SQLConf.CONTENT));
        List<Article> blogList = articleService.list(queryWrapper);

        blogList.forEach(item -> {
            String blogSortUid = item.getArticleSortUid();
            if (map.get(blogSortUid) != null) {
                Integer count = map.get(blogSortUid) + 1;
                map.put(blogSortUid, count);
            } else {
                map.put(blogSortUid, 0);
            }
        });

        articleSortList.forEach(item -> {
            item.setSort(map.get(item.getUid()));
            item.setUpdateTime(new Date());
        });
        articleSortService.updateBatchById(articleSortList);
        return ResultUtil.successWithMessage(MessageConf.OPERATION_SUCCESS);
    }

    @Override
    public ArticleSort getTopOne() {
        QueryWrapper<ArticleSort> blogSortQueryWrapper = new QueryWrapper<>();
        blogSortQueryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        blogSortQueryWrapper.last(SysConf.LIMIT_ONE);
        blogSortQueryWrapper.orderByDesc(SQLConf.SORT);
        ArticleSort articleSort = articleSortService.getOne(blogSortQueryWrapper);
        return articleSort;
    }
}
