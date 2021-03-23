package com.example.creation.web.restapi;


import com.example.creation.commons.entity.Link;
import com.example.creation.commons.entity.Tag;
import com.example.creation.utils.JsonUtils;
import com.example.creation.utils.RedisUtil;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.web.global.MessageConf;
import com.example.creation.web.global.SysConf;
import com.example.creation.web.log.BussinessLog;
import com.example.creation.web.requestLimit.RequestLimit;
import com.example.creation.xo.global.RedisConf;
import com.example.creation.xo.service.*;
import com.example.creation.base.enums.EBehavior;
import com.example.creation.base.global.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 首页 RestApi
 *
 */
@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
@Slf4j
public class IndexRestApi {

    @Resource
    private TagService tagService;

    @Resource
    private LinkService linkService;

    @Resource
    private WebConfigService webConfigService;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private ArticleService articleService;

    @Resource
    private WebNavbarService webNavbarService;

    @Resource
    private RedisUtil redisUtil;

    @RequestLimit(amount = 200, time = 60000)
    @ApiOperation(value = "通过推荐等级获取文章列表", notes = "通过推荐等级获取文章列表")
    @GetMapping("/getBlogByLevel")
    public String getBlogByLevel(HttpServletRequest request,
                                 @ApiParam(name = "level", value = "推荐等级", required = false) @RequestParam(name = "level", required = false, defaultValue = "0") Integer level,
                                 @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                 @ApiParam(name = "useSort", value = "使用排序", required = false) @RequestParam(name = "useSort", required = false, defaultValue = "0") Integer useSort) {

        return ResultUtil.result(SysConf.SUCCESS, articleService.getBlogPageByLevel(level, currentPage, useSort));
    }

    @ApiOperation(value = "获取首页排行文章", notes = "获取首页排行文章")
    @GetMapping("/getHotBlog")
    public String getHotBlog() {

        log.info("获取首页排行文章");
        return ResultUtil.result(SysConf.SUCCESS, articleService.getHotBlog());
    }

    @ApiOperation(value = "获取首页最新的文章", notes = "获取首页最新的文章")
    @GetMapping("/getNewBlog")
    public String getNewBlog(HttpServletRequest request,
                             @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                             @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        log.info("获取首页最新的文章");
        return ResultUtil.result(SysConf.SUCCESS, articleService.getNewBlog(currentPage, null));
    }

    @ApiOperation(value = "mogu-search调用获取文章的接口[包含内容]", notes = "mogu-search调用获取文章的接口")
    @GetMapping("/getBlogBySearch")
    public String getBlogBySearch(HttpServletRequest request,
                                  @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                  @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        log.info("获取首页最新的文章");
        return ResultUtil.result(SysConf.SUCCESS, articleService.getBlogBySearch(currentPage, null));
    }


    @ApiOperation(value = "按时间戳获取文章", notes = "按时间戳获取文章")
    @GetMapping("/getBlogByTime")
    public String getBlogByTime(HttpServletRequest request,
                                @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        String blogNewCount = sysParamsService.getSysParamsValueByKey(SysConf.BLOG_NEW_COUNT);
        return ResultUtil.result(SysConf.SUCCESS, articleService.getBlogByTime(currentPage, Long.valueOf(blogNewCount)));
    }

    @ApiOperation(value = "获取最热标签", notes = "获取最热标签")
    @GetMapping("/getHotTag")
    public String getHotTag() {
        String hotTagCount = sysParamsService.getSysParamsValueByKey(SysConf.HOT_TAG_COUNT);
        // 从Redis中获取友情链接
        String jsonResult = redisUtil.get(RedisConf.BLOG_TAG + Constants.SYMBOL_COLON + hotTagCount);
        if(StringUtils.isNotEmpty(jsonResult)) {
            List jsonResult2List = JsonUtils.jsonArrayToArrayList(jsonResult);
            return ResultUtil.result(SysConf.SUCCESS, jsonResult2List);
        }
        List<Tag> tagList = tagService.getHotTag(Integer.valueOf(hotTagCount));
        if(tagList.size() > 0) {
            redisUtil.setEx(RedisConf.BLOG_TAG + Constants.SYMBOL_COLON + hotTagCount ,JsonUtils.objectToJson(tagList), 1, TimeUnit.HOURS);
        }
        return ResultUtil.result(SysConf.SUCCESS, tagList);
    }

    @ApiOperation(value = "获取友情链接", notes = "获取友情链接")
    @GetMapping("/getLink")
    public String getLink() {
        String friendlyLinkCount = sysParamsService.getSysParamsValueByKey(SysConf.FRIENDLY_LINK_COUNT);
        // 从Redis中获取友情链接
        String jsonResult = redisUtil.get(RedisConf.BLOG_LINK + Constants.SYMBOL_COLON + friendlyLinkCount);
        if(StringUtils.isNotEmpty(jsonResult)) {
            List jsonResult2List = JsonUtils.jsonArrayToArrayList(jsonResult);
            return ResultUtil.result(SysConf.SUCCESS, jsonResult2List);
        }
        List<Link> linkList = linkService.getListByPageSize(Integer.valueOf(friendlyLinkCount));
        if(linkList.size() > 0) {
            redisUtil.setEx(RedisConf.BLOG_LINK + Constants.SYMBOL_COLON + friendlyLinkCount ,JsonUtils.objectToJson(linkList), 1, TimeUnit.HOURS);
        }
        return ResultUtil.result(SysConf.SUCCESS, linkList);
    }

    @BussinessLog(value = "点击友情链接", behavior = EBehavior.FRIENDSHIP_LINK)
    @ApiOperation(value = "增加友情链接点击数", notes = "增加友情链接点击数")
    @GetMapping("/addLinkCount")
    public String addLinkCount(@ApiParam(name = "uid", value = "友情链接UID", required = false) @RequestParam(name = "uid", required = false) String uid) {
        log.info("点击友链");
        return linkService.addLinkCount(uid);
    }

    @ApiOperation(value = "获取网站配置", notes = "获取友情链接")
    @GetMapping("/getWebConfig")
    public String getWebConfig() {
        log.info("获取网站配置");
        return ResultUtil.result(SysConf.SUCCESS, webConfigService.getWebConfigByShowList());
    }

    @ApiOperation(value = "获取网站导航栏", notes = "获取网站导航栏")
    @GetMapping("/getWebNavbar")
    public String getWebNavbar() {
        log.info("获取网站导航栏");
        return ResultUtil.result(SysConf.SUCCESS, webNavbarService.getAllList());
    }

    @BussinessLog(value = "记录访问页面", behavior = EBehavior.VISIT_PAGE)
    @ApiOperation(value = "记录访问页面", notes = "记录访问页面")
    @GetMapping("/recorderVisitPage")
    public String recorderVisitPage(@ApiParam(name = "pageName", value = "页面名称", required = false) @RequestParam(name = "pageName", required = true) String pageName) {

        if (StringUtils.isEmpty(pageName)) {
            return ResultUtil.result(SysConf.SUCCESS, MessageConf.PARAM_INCORRECT);
        }
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.INSERT_SUCCESS);
    }
}

