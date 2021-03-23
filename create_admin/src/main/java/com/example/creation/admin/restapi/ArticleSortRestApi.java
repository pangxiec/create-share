package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.base.exception.ThrowableUtils;
import com.example.creation.base.validator.group.Delete;
import com.example.creation.base.validator.group.GetList;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.ArticleSortService;
import com.example.creation.xo.vo.ArticleSortVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xmy
 * @date 2021/3/17 11:44
 */
@RestController
@RequestMapping("/articleSort")
@Api(value = "文章分类相关接口", tags = {"文章分类相关接口"})
@Slf4j
public class ArticleSortRestApi {

    @Resource
    private ArticleSortService articleSortService;

    @AuthorityVerify
    @ApiOperation(value = "获取文章分类列表", notes = "获取文章分类列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody ArticleSortVO articleSortVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("获取文章分类列表");
        return ResultUtil.successWithData(articleSortService.getPageList(articleSortVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加文章分类")
    @ApiOperation(value = "增加文章分类", notes = "增加文章分类", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody ArticleSortVO articleSortVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("增加文章分类");
        return articleSortService.addArticleSort(articleSortVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑文章分类")
    @ApiOperation(value = "编辑文章分类", notes = "编辑文章分类", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody ArticleSortVO articleSortVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑文章分类");
        return articleSortService.editBlogSort(articleSortVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "批量删除文章分类")
    @ApiOperation(value = "批量删除文章分类", notes = "批量删除文章分类", response = String.class)
    @PostMapping("/deleteBatch")
    public String delete(@Validated({Delete.class}) @RequestBody List<ArticleSortVO> articleSortVoList, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("批量删除文章分类");
        return articleSortService.deleteBatchBlogSort(articleSortVoList);
    }

    @AuthorityVerify
    @ApiOperation(value = "置顶分类", notes = "置顶分类", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody ArticleSortVO articleSortVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("置顶分类");
        return articleSortService.stickBlogSort(articleSortVO);

    }

    @AuthorityVerify
    @OperationLogger(value = "通过点击量排序文章分类")
    @ApiOperation(value = "通过点击量排序文章分类", notes = "通过点击量排序文章分类", response = String.class)
    @PostMapping("/blogArticleByClickCount")
    public String blogSortByClickCount() {
        log.info("通过点击量排序文章分类");
        return articleSortService.blogSortByClickCount();
    }

    /**
     * 通过引用量排序标签
     * 引用量就是所有的文章中，有多少使用了该标签，如果使用的越多，该标签的引用量越大，那么排名越靠前
     *
     * @return
     */
    @AuthorityVerify
    @OperationLogger(value = "通过引用量排序文章分类")
    @ApiOperation(value = "通过引用量排序文章分类", notes = "通过引用量排序文章分类", response = String.class)
    @PostMapping("/blogSortByCite")
    public String blogSortByCite() {
        log.info("通过引用量排序文章分类");
        return articleSortService.blogSortByCite();
    }
}

