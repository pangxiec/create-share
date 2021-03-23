package com.example.creation.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.creation.commons.entity.Article;
import com.example.creation.commons.entity.SubjectItem;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.xo.global.MessageConf;
import com.example.creation.xo.global.SQLConf;
import com.example.creation.xo.mapper.SubjectItemMapper;
import com.example.creation.xo.service.ArticleService;
import com.example.creation.xo.service.SubjectItemService;
import com.example.creation.xo.vo.SubjectItemVO;
import com.example.creation.base.enums.EStatus;
import com.example.creation.base.exception.exceptionType.DeleteException;
import com.example.creation.base.global.BaseSQLConf;
import com.example.creation.base.global.ErrorCode;
import com.example.creation.base.serviceImpl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SubjectItemServiceImpl extends SuperServiceImpl<SubjectItemMapper, SubjectItem> implements SubjectItemService {

    @Resource
    SubjectItemService subjectItemService;
    @Resource
    ArticleService articleService;

    @Override
    public IPage<SubjectItem> getPageList(SubjectItemVO subjectItemVO) {
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper<>();
        Page<SubjectItem> page = new Page<>();
        if (StringUtils.isNotEmpty(subjectItemVO.getSubjectUid())) {
            queryWrapper.eq(BaseSQLConf.SUBJECT_UID, subjectItemVO.getSubjectUid());
        }
        page.setCurrent(subjectItemVO.getCurrentPage());
        page.setSize(subjectItemVO.getPageSize());
        queryWrapper.eq(BaseSQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(BaseSQLConf.SORT);
        IPage<SubjectItem> pageList = subjectItemService.page(page, queryWrapper);
        List<SubjectItem> subjectItemList = pageList.getRecords();
        List<String> articleUidList = new ArrayList<>();
        subjectItemList.forEach(item -> {
            articleUidList.add(item.getBlogUid());
        });
        Collection<Article> blogCollection = null;
        if (articleUidList.size() > 0) {
            blogCollection = articleService.listByIds(articleUidList);
            if (blogCollection.size() > 0) {
                List<Article> blogTempList = new ArrayList<>(blogCollection);
                List<Article> blogList = articleService.setTagAndSortAndPictureByBlogList(blogTempList);
                Map<String, Article> blogMap = new HashMap<>();
                blogList.forEach(item -> {
                    blogMap.put(item.getUid(), item);
                });
                subjectItemList.forEach(item -> {
                    item.setArticle(blogMap.get(item.getBlogUid()));
                });
                pageList.setRecords(subjectItemList);
            }
        }
        return pageList;
    }

    @Override
    public String addSubjectItemList(List<SubjectItemVO> subjectItemVOList) {
        List<String> blogUidList = new ArrayList<>();
        String subjectUid = "";
        for (SubjectItemVO subjectItemVO : subjectItemVOList) {
            blogUidList.add(subjectItemVO.getBlogUid());
            if (StringUtils.isEmpty(subjectUid) && StringUtils.isNotEmpty(subjectItemVO.getSubjectUid())) {
                subjectUid = subjectItemVO.getSubjectUid();
            }
        }
        // 查询SubjectItem中是否包含重复的文章
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.SUBJECT_UID, subjectUid);
        queryWrapper.in(SQLConf.BLOG_UID, blogUidList);
        List<SubjectItem> repeatSubjectItemList = subjectItemService.list(queryWrapper);
        // 找出重复的文章UID
        List<String> repeatBlogList = new ArrayList<>();
        repeatSubjectItemList.forEach(item -> {
            repeatBlogList.add(item.getBlogUid());
        });

        List<SubjectItem> subjectItemList = new ArrayList<>();
        for (SubjectItemVO subjectItemVO : subjectItemVOList) {
            if (StringUtils.isEmpty(subjectItemVO.getSubjectUid()) || StringUtils.isEmpty(subjectItemVO.getBlogUid())) {
                return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
            }
            // 判断是否重复添加
            if (repeatBlogList.contains(subjectItemVO.getBlogUid())) {
                continue;
            } else {
                SubjectItem subjectItem = new SubjectItem();
                subjectItem.setSubjectUid(subjectItemVO.getSubjectUid());
                subjectItem.setBlogUid(subjectItemVO.getBlogUid());
                subjectItem.setStatus(EStatus.ENABLE);
                subjectItemList.add(subjectItem);
            }
        }

        if (subjectItemList.size() <= 0) {
            if (repeatBlogList.size() == 0) {
                return ResultUtil.errorWithMessage(MessageConf.INSERT_FAIL);
            } else {
                return ResultUtil.errorWithMessage(MessageConf.INSERT_FAIL + "，已跳过" + repeatBlogList.size() + "个重复数据");
            }
        } else {
            subjectItemService.saveBatch(subjectItemList);
            if (repeatBlogList.size() == 0) {
                return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS);
            } else {
                return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS + "，已跳过" + repeatBlogList.size() + "个重复数据，成功插入" + (subjectItemVOList.size() - repeatBlogList.size()) + "条数据");
            }
        }
    }

    @Override
    public String editSubjectItemList(List<SubjectItemVO> subjectItemVOList) {
        List<String> subjectItemUidList = new ArrayList<>();
        subjectItemVOList.forEach(item -> {
            subjectItemUidList.add(item.getUid());
        });
        Collection<SubjectItem> subjectItemCollection = null;
        if (subjectItemUidList.size() > 0) {
            subjectItemCollection = subjectItemService.listByIds(subjectItemUidList);
            if (subjectItemCollection.size() > 0) {
                HashMap<String, SubjectItemVO> subjectItemVOHashMap = new HashMap<>();
                subjectItemVOList.forEach(item -> {
                    subjectItemVOHashMap.put(item.getUid(), item);
                });
                // 修改排序字段
                List<SubjectItem> subjectItemList = new ArrayList<>();
                subjectItemCollection.forEach(item -> {
                    SubjectItemVO subjectItemVO = subjectItemVOHashMap.get(item.getUid());
                    item.setSubjectUid(subjectItemVO.getSubjectUid());
                    item.setBlogUid(subjectItemVO.getBlogUid());
                    item.setStatus(EStatus.ENABLE);
                    item.setSort(subjectItemVO.getSort());
                    item.setUpdateTime(new Date());
                    subjectItemList.add(item);
                });
                subjectItemService.updateBatchById(subjectItemList);
            }
        }
        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    @Override
    public String deleteBatchSubjectItem(List<SubjectItemVO> subjectItemVOList) {
        if (subjectItemVOList.size() <= 0) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();
        subjectItemVOList.forEach(item -> {
            uids.add(item.getUid());
        });
        subjectItemService.removeByIds(uids);
        return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
    }

    @Override
    public String deleteBatchSubjectItemByBlogUid(List<String> blogUid) {
        boolean checkSuccess = StringUtils.checkUidList(blogUid);
        if (!checkSuccess) {
            throw new DeleteException(ErrorCode.DELETE_FAILED_PLEASE_CHECK_UID, MessageConf.DELETE_FAILED_PLEASE_CHECK_UID);
        }
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(SQLConf.BLOG_UID, blogUid);
        subjectItemService.remove(queryWrapper);
        return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
    }

    @Override
    public String sortByCreateTime(String subjectUid, Boolean isDesc) {
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.eq(SQLConf.SUBJECT_UID, subjectUid);
        // 查询出所有的专题列表
        List<SubjectItem> subjectItemList = subjectItemService.list(queryWrapper);
        // 获取专题中的文章uid
        List<String> blogUidList = new ArrayList<>();
        subjectItemList.forEach(item -> {
            blogUidList.add(item.getBlogUid());
        });
        if(blogUidList.size() == 0) {
            return ResultUtil.errorWithMessage(MessageConf.UPDATE_FAIL);
        }
        Collection<Article> blogList = articleService.listByIds(blogUidList);
        List<Article> tempBlogList = new ArrayList<>();
        // 升序排列或降序排列
        if(isDesc) {
            tempBlogList = blogList.stream().sorted(Comparator.comparing(Article::getCreateTime).reversed()).collect(Collectors.toList());
        } else {
            tempBlogList = blogList.stream().sorted(Comparator.comparing(Article::getCreateTime)).collect(Collectors.toList());
        }

        // 设置初始化最大的sort值
        int maxSort = tempBlogList.size();
        Map<String, Integer> subjectItemSortMap = new HashMap<>();
        for (Article item : tempBlogList) {
            subjectItemSortMap.put(item.getUid(), maxSort--);
        }

        // 设置更新后的排序值
        for (SubjectItem item : subjectItemList) {
            item.setSort(subjectItemSortMap.get(item.getBlogUid()));
        }
        subjectItemService.updateBatchById(subjectItemList);
        return ResultUtil.successWithMessage(MessageConf.OPERATION_SUCCESS);
    }
}
