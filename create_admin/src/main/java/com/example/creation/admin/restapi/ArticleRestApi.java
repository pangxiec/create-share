package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.ArticleService;
import com.example.creation.xo.vo.ArticleVO;
import com.example.creation.base.exception.ThrowableUtils;
import com.example.creation.base.validator.group.Delete;
import com.example.creation.base.validator.group.GetList;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author xmy
 * @date 2021/3/15 14:32
 */
@RestController
@RequestMapping("/blog")
@Api(value = "文章相关接口", tags = {"文章相关接口"})
@Slf4j
public class ArticleRestApi {

    @Resource
    private ArticleService articleService;

    @AuthorityVerify
    @ApiOperation(value = "获取文章列表", notes = "获取文章列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody ArticleVO articleVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(articleService.getPageList(articleVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加文章")
    @ApiOperation(value = "增加文章", notes = "增加文章", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody ArticleVO articleVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return articleService.addArticle(articleVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑文章")
    @ApiOperation(value = "编辑文章", notes = "编辑文章", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody ArticleVO articleVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return articleService.editArticle(articleVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "推荐文章排序调整")
    @ApiOperation(value = "推荐文章排序调整", notes = "推荐文章排序调整", response = String.class)
    @PostMapping("/editBatch")
    public String editBatch(@RequestBody List<ArticleVO> articleVOList) {
        return articleService.editBatch(articleVOList);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除文章")
    @ApiOperation(value = "删除文章", notes = "删除文章", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody ArticleVO articleVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return articleService.deleteArticle(articleVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除选中文章")
    @ApiOperation(value = "删除选中文章", notes = "删除选中文章", response = String.class)
    @PostMapping("/deleteBatch")
    public String deleteBatch(@RequestBody List<ArticleVO> articleVoList) {
        return articleService.deleteBatchArticle(articleVoList);
    }

}