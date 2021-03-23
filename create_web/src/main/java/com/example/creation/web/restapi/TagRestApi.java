package com.example.creation.web.restapi;


import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.web.global.SysConf;
import com.example.creation.web.log.BussinessLog;
import com.example.creation.xo.service.ArticleService;
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
 * 标签RestApi
 *
 */
@RestController
@RequestMapping("/tag")
@Api(value = "文章标签相关接口", tags = {"文章标签相关接口"})
@Slf4j
public class TagRestApi {

    @Resource
    private ArticleService articleService;

    @Resource
    private TagService tagService;

    /**
     * 获取标签的信息
     *
     * @return
     */
    @ApiOperation(value = "获取标签的信息", notes = "获取标签的信息")
    @GetMapping("/getTagList")
    public String getTagList() {
        log.info("获取标签信息");
        return ResultUtil.result(SysConf.SUCCESS, tagService.getList());
    }

    /**
     * 通过TagUid获取文章
     *
     * @param request
     * @param currentPage
     * @param pageSize
     * @return
     */
    @BussinessLog(value = "点击标签", behavior = EBehavior.VISIT_TAG)
    @ApiOperation(value = "通过TagUid获取文章", notes = "通过TagUid获取文章")
    @GetMapping("/getArticleByTagUid")
    public String getArticleByTagUid(HttpServletRequest request,
                                     @ApiParam(name = "tagUid", value = "标签UID", required = false) @RequestParam(name = "tagUid", required = false) String tagUid,
                                     @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                     @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(tagUid)) {
            return ResultUtil.result(SysConf.ERROR, "传入TagUid不能为空");
        }
        log.info("通过blogSortUid获取文章列表");
        return ResultUtil.result(SysConf.SUCCESS, articleService.searchBlogByTag(tagUid, currentPage, pageSize));
    }

}

