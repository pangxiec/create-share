package com.example.creation.admin.restapi;

import com.example.creation.admin.global.SysConf;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.ArticleService;
import com.example.creation.xo.service.CommentService;
import com.example.creation.xo.service.UserService;
import com.example.creation.xo.service.WebVisitService;
import com.example.creation.base.enums.EStatus;
import com.example.creation.base.global.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
@Slf4j
public class IndexRestApi {

    @Resource
    private ArticleService articleService;

    @Resource
    private CommentService commentService;

    @Resource
    private WebVisitService webVisitService;

    @Resource
    private UserService userService;

    @ApiOperation(value = "首页初始化数据", notes = "首页初始化数据", response = String.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String init() {
        Map<String, Object> map = new HashMap<>(Constants.NUM_FOUR);
        map.put(SysConf.BLOG_COUNT, articleService.getArticleCount(EStatus.ENABLE));
        map.put(SysConf.COMMENT_COUNT, commentService.getCommentCount(EStatus.ENABLE));
        map.put(SysConf.USER_COUNT, userService.getUserCount(EStatus.ENABLE));
        map.put(SysConf.VISIT_COUNT, webVisitService.getWebVisitCount());
        return ResultUtil.result(SysConf.SUCCESS, map);
    }

    @ApiOperation(value = "获取最近一周用户独立IP数和访问量", notes = "获取最近一周用户独立IP数和访问量", response = String.class)
    @RequestMapping(value = "/getVisitByWeek", method = RequestMethod.GET)
    public String getVisitByWeek() {
        Map<String, Object> visitByWeek = webVisitService.getVisitByWeek();
        return ResultUtil.result(SysConf.SUCCESS, visitByWeek);
    }

    @ApiOperation(value = "获取每个标签下文章数目", notes = "获取每个标签下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByTag", method = RequestMethod.GET)
    public String getArticleCountByTag() {
        List<Map<String, Object>> blogCountByTag = articleService.getArticleCountByTag();
        return ResultUtil.result(SysConf.SUCCESS, blogCountByTag);
    }

    @ApiOperation(value = "获取每个分类下文章数目", notes = "获取每个分类下文章数目", response = String.class)
    @RequestMapping(value = "/getBlogCountByBlogSort", method = RequestMethod.GET)
    public String getArticleCountByArticleSort() {
        List<Map<String, Object>> blogCountByTag = articleService.getArticleCountByBlogSort();
        return ResultUtil.result(SysConf.SUCCESS, blogCountByTag);
    }

    @ApiOperation(value = "获取一年内的文章创作数", notes = "获取一年内的文章贡献度", response = String.class)
    @RequestMapping(value = "/getBlogContributeCount", method = RequestMethod.GET)
    public String getArticleContributeCount() {
        Map<String, Object> resultMap = articleService.getArticleContributeCount();
        return ResultUtil.result(SysConf.SUCCESS, resultMap);
    }


}
