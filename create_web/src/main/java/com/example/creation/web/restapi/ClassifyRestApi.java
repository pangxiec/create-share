package com.example.creation.web.restapi;


import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.web.global.SysConf;
import com.example.creation.web.log.BussinessLog;
import com.example.creation.xo.service.ArticleService;
import com.example.creation.xo.service.ArticleSortService;
import com.example.creation.xo.service.TagService;
import com.example.creation.base.enums.EBehavior;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 分类RestApi
 *
 */
@RestController
@RequestMapping("/classify")
@Api(value = "分类相关接口", tags = {"分类相关接口"})
@Slf4j
public class ClassifyRestApi {

    @Resource
    ArticleService articleService;

    @Resource
    TagService tagService;

    @Resource
    ArticleSortService articleSortService;

    /**
     * 获取分类的信息
     */
    @ApiOperation(value = "获取分类的信息", notes = "获取分类的信息")
    @GetMapping("/getBlogSortList")
    public String getBlogSortList() {
        log.info("获取分类信息");
        return ResultUtil.result(SysConf.SUCCESS, articleSortService.getList());
    }

    @BussinessLog(value = "点击分类", behavior = EBehavior.VISIT_CLASSIFY)
    @ApiOperation(value = "通过blogSortUid获取文章", notes = "通过blogSortUid获取文章")
    @GetMapping("/getArticleByBlogSortUid")
    public String getArticleByBlogSortUid(HttpServletRequest request,
                                          @ApiParam(name = "blogSortUid", value = "分类UID", required = false) @RequestParam(name = "blogSortUid", required = false) String blogSortUid,
                                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(blogSortUid)) {
            log.info("点击分类,传入BlogSortUid不能为空");
            return ResultUtil.result(SysConf.ERROR, "传入BlogSortUid不能为空");
        }
        log.info("通过blogSortUid获取文章列表");
        return ResultUtil.result(SysConf.SUCCESS, articleService.getListByBlogSortUid(blogSortUid, currentPage, pageSize));
    }

}

