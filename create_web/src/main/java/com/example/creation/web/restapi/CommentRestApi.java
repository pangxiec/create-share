package com.example.creation.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.creation.base.enums.*;
import com.example.creation.commons.entity.*;
import com.example.creation.commons.feign.PictureFeignClient;
import com.example.creation.utils.JsonUtils;
import com.example.creation.utils.RedisUtil;
import com.example.creation.utils.ResultUtil;
import com.example.creation.utils.StringUtils;
import com.example.creation.web.global.MessageConf;
import com.example.creation.web.global.RedisConf;
import com.example.creation.web.global.SQLConf;
import com.example.creation.web.global.SysConf;
import com.example.creation.web.log.BussinessLog;
import com.example.creation.xo.service.*;
import com.example.creation.xo.utils.RabbitMqUtil;
import com.example.creation.xo.utils.WebUtil;
import com.example.creation.xo.vo.CommentVO;
import com.example.creation.xo.vo.UserVO;
import com.example.creation.base.exception.ThrowableUtils;
import com.example.creation.base.global.BaseSysConf;
import com.example.creation.base.global.Constants;
import com.example.creation.base.holder.RequestHolder;
import com.example.creation.base.validator.group.Delete;
import com.example.creation.base.validator.group.GetList;
import com.example.creation.base.validator.group.GetOne;
import com.example.creation.base.validator.group.Insert;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ??????RestApi
 *
 */
@RestController
@RefreshScope
@RequestMapping("/web/comment")
@Api(value = "??????????????????", tags = {"??????????????????"})
@Slf4j
public class CommentRestApi {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private WebUtil webUtil;
    @Resource
    private WebConfigService webConfigService;
    @Resource
    private SystemConfigService systemConfigService;
    @Resource
    private RabbitMqUtil rabbitMqUtil;
    @Resource
    private ArticleService articleService;
    @Resource
    private CommentService commentService;
    @Resource
    private UserService userService;
    @Resource
    private PictureFeignClient pictureFeignClient;
    @Resource
    private CommentReportService commentReportService;
    @Value(value = "${BLOG.USER_TOKEN_SURVIVAL_TIME}")
    private Long userTokenSurvivalTime;
    @Value(value = "${data.website.url}")
    private String dataWebsiteUrl;

    /**
     * ??????????????????
     *
     * @param commentVO
     * @param result
     * @return
     */
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        ThrowableUtils.checkParamArgument(result);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(commentVO.getBlogUid())) {
            queryWrapper.like(SQLConf.BLOG_UID, commentVO.getBlogUid());
        }
        queryWrapper.eq(SQLConf.SOURCE, commentVO.getSource());

        //??????
        Page<Comment> page = new Page<>();
        page.setCurrent(commentVO.getCurrentPage());
        page.setSize(commentVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.isNull(SQLConf.TO_UID);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(SQLConf.TYPE, ECommentType.COMMENT);
        // ???????????????????????????????????????????????????
        IPage<Comment> pageList = commentService.page(page, queryWrapper);
        List<Comment> list = pageList.getRecords();
        List<String> firstUidList = new ArrayList<>();
        list.forEach(item -> {
            firstUidList.add(item.getUid());
        });

        if (firstUidList.size() > 0) {
            // ?????????????????????????????????
            QueryWrapper<Comment> notFirstQueryWrapper = new QueryWrapper<>();
            notFirstQueryWrapper.in(SQLConf.FIRST_COMMENT_UID, firstUidList);
            notFirstQueryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
            List<Comment> notFirstList = commentService.list(notFirstQueryWrapper);
            // ?????????????????????????????????
            if (notFirstList.size() > 0) {
                list.addAll(notFirstList);
            }
        }

        List<String> userUidList = new ArrayList<>();
        list.forEach(item -> {
            String userUid = item.getUserUid();
            String toUserUid = item.getToUserUid();
            if (StringUtils.isNotEmpty(userUid)) {
                userUidList.add(item.getUserUid());
            }
            if (StringUtils.isNotEmpty(toUserUid)) {
                userUidList.add(item.getToUserUid());
            }
        });
        Collection<User> userList = new ArrayList<>();
        if (userUidList.size() > 0) {
            userList = userService.listByIds(userUidList);
        }

        // ??????????????????????????????
        List<User> filterUserList = new ArrayList<>();
        userList.forEach(item -> {
            User user = new User();
            user.setAvatar(item.getAvatar());
            user.setUid(item.getUid());
            user.setNickName(item.getNickName());
            user.setUserTag(item.getUserTag());
            filterUserList.add(user);
        });

        // ??????????????????
        StringBuffer fileUids = new StringBuffer();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.append(item.getAvatar() + SysConf.FILE_SEGMENTATION);
            }
        });
        String pictureList = null;
        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);
        Map<String, String> pictureMap = new HashMap<>();
        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
        });

        Map<String, User> userMap = new HashMap<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar()) && pictureMap.get(item.getAvatar()) != null) {
                item.setPhotoUrl(pictureMap.get(item.getAvatar()));
            }
            userMap.put(item.getUid(), item);
        });

        Map<String, Comment> commentMap = new HashMap<>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getUserUid())) {
                item.setUser(userMap.get(item.getUserUid()));
            }
            if (StringUtils.isNotEmpty(item.getToUserUid())) {
                item.setToUser(userMap.get(item.getToUserUid()));
            }
            commentMap.put(item.getUid(), item);
        });

        // ?????????????????????????????????
        Map<String, List<Comment>> toCommentListMap = new HashMap<>();
        for (int a = 0; a < list.size(); a++) {
            List<Comment> tempList = new ArrayList<>();
            for (int b = 0; b < list.size(); b++) {
                if (list.get(a).getUid().equals(list.get(b).getToUid())) {
                    tempList.add(list.get(b));
                }
            }
            toCommentListMap.put(list.get(a).getUid(), tempList);
        }
        List<Comment> firstComment = new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isEmpty(item.getToUid())) {
                firstComment.add(item);
            }
        });
        pageList.setRecords(getCommentReplys(firstComment, toCommentListMap));
        return ResultUtil.result(SysConf.SUCCESS, pageList);
    }

    @ApiOperation(value = "App?????????????????????", notes = "??????????????????")
    @PostMapping("/getListByApp")
    public String getListByApp(@Validated({GetList.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        ThrowableUtils.checkParamArgument(result);

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(commentVO.getBlogUid())) {
            queryWrapper.like(SQLConf.BLOG_UID, commentVO.getBlogUid());
        }
        queryWrapper.eq(SQLConf.SOURCE, commentVO.getSource());
        //??????
        Page<Comment> page = new Page<>();
        page.setCurrent(commentVO.getCurrentPage());
        page.setSize(commentVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        queryWrapper.eq(SQLConf.TYPE, ECommentType.COMMENT);
        // ????????????????????????????????????
        IPage<Comment> pageList = commentService.page(page, queryWrapper);
        List<Comment> list = pageList.getRecords();
        List<String> toCommentUidList = new ArrayList<>();
        // ?????????????????????UID
        list.forEach(item -> {
            toCommentUidList.add(item.getToUid());
        });

        // ????????????????????????????????????????????????
        List<Comment> allCommentList = new ArrayList<>();
        allCommentList.addAll(list);

        // ????????????????????????
        Collection<Comment> toCommentList = null;
        if (toCommentUidList.size() > 0) {
            toCommentList = commentService.listByIds(toCommentUidList);
            allCommentList.addAll(toCommentList);
        }

        // ????????????????????????????????????
        List<String> userUidList = new ArrayList<>();
        allCommentList.forEach(item -> {
            String userUid = item.getUserUid();
            String toUserUid = item.getToUserUid();
            if (StringUtils.isNotEmpty(userUid)) {
                userUidList.add(item.getUserUid());
            }
            if (StringUtils.isNotEmpty(toUserUid)) {
                userUidList.add(item.getToUserUid());
            }
        });
        Collection<User> userList = new ArrayList<>();
        if (userUidList.size() > 0) {
            userList = userService.listByIds(userUidList);
        }

        // ??????????????????????????????
        List<User> filterUserList = new ArrayList<>();
        userList.forEach(item -> {
            User user = new User();
            user.setAvatar(item.getAvatar());
            user.setUid(item.getUid());
            user.setNickName(item.getNickName());
            user.setUserTag(item.getUserTag());
            filterUserList.add(user);
        });

        // ??????????????????
        StringBuffer fileUids = new StringBuffer();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.append(item.getAvatar() + SysConf.FILE_SEGMENTATION);
            }
        });
        String pictureList = null;
        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);
        Map<String, String> pictureMap = new HashMap<>();
        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
        });

        Map<String, User> userMap = new HashMap<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar()) && pictureMap.get(item.getAvatar()) != null) {
                item.setPhotoUrl(pictureMap.get(item.getAvatar()));
            }
            userMap.put(item.getUid(), item);
        });

        // ??????????????????Map?????????
        Map<String, Comment> commentMap = new HashMap<>();
        allCommentList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getUserUid())) {
                item.setUser(userMap.get(item.getUserUid()));
            }
            if (StringUtils.isNotEmpty(item.getToUserUid())) {
                item.setToUser(userMap.get(item.getToUserUid()));
            }
            commentMap.put(item.getUid(), item);
        });

        // ??????????????????????????????????????????
        List<Comment> returnCommentList = new ArrayList<>();
        list.forEach(item -> {
            String commentUid = item.getUid();
            String toCommentUid = item.getToUid();
            Comment comment = commentMap.get(commentUid);
            if (StringUtils.isNotEmpty(toCommentUid)) {
                comment.setToComment(commentMap.get(toCommentUid));
            }
            returnCommentList.add(comment);
        });
        pageList.setRecords(returnCommentList);
        return ResultUtil.result(SysConf.SUCCESS, pageList);
    }

    @ApiOperation(value = "????????????????????????????????????", notes = "???????????????????????????")
    @PostMapping("/getListByUser")
    public String getListByUser(HttpServletRequest request, @Validated({GetList.class}) @RequestBody UserVO userVO) {

        if (request.getAttribute(SysConf.USER_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String requestUserUid = request.getAttribute(SysConf.USER_UID).toString();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        //??????
        Page<Comment> page = new Page<>();
        page.setCurrent(userVO.getCurrentPage());
        page.setSize(userVO.getPageSize());
        queryWrapper.eq(SQLConf.TYPE, ECommentType.COMMENT);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        // ????????? ???????????? ??? ????????????
        queryWrapper.and(wrapper -> wrapper.eq(SQLConf.USER_UID, requestUserUid).or().eq(SQLConf.TO_USER_UID, requestUserUid));
        IPage<Comment> pageList = commentService.page(page, queryWrapper);
        List<Comment> list = pageList.getRecords();
        List<String> userUidList = new ArrayList<>();
        list.forEach(item -> {
            String userUid = item.getUserUid();
            String toUserUid = item.getToUserUid();
            if (StringUtils.isNotEmpty(userUid)) {
                userUidList.add(item.getUserUid());
            }
            if (StringUtils.isNotEmpty(toUserUid)) {
                userUidList.add(item.getToUserUid());
            }
        });

        // ??????????????????
        Collection<User> userList = new ArrayList<>();
        if (userUidList.size() > 0) {
            userList = userService.listByIds(userUidList);
        }
        // ??????????????????????????????
        List<User> filterUserList = new ArrayList<>();
        userList.forEach(item -> {
            User user = new User();
            user.setAvatar(item.getAvatar());
            user.setUid(item.getUid());
            user.setNickName(item.getNickName());
            filterUserList.add(user);
        });
        // ??????????????????
        StringBuffer fileUids = new StringBuffer();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.append(item.getAvatar() + SysConf.FILE_SEGMENTATION);
            }
        });
        String pictureList = null;
        if (fileUids != null) {
            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);
        Map<String, String> pictureMap = new HashMap<>();
        picList.forEach(item -> {
            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
        });

        Map<String, User> userMap = new HashMap<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar()) && pictureMap.get(item.getAvatar()) != null) {
                item.setPhotoUrl(pictureMap.get(item.getAvatar()));
            }
            userMap.put(item.getUid(), item);
        });

        // ???????????????????????? ???????????? ??? ????????????
        List<Comment> commentList = new ArrayList<>();
        List<Comment> replyList = new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getUserUid())) {
                item.setUser(userMap.get(item.getUserUid()));
            }

            if (StringUtils.isNotEmpty(item.getToUserUid())) {
                item.setToUser(userMap.get(item.getToUserUid()));
            }
            // ??????sourceName
            if (StringUtils.isNotEmpty(item.getSource())) {
                item.setSourceName(ECommentSource.valueOf(item.getSource()).getName());
            }
            if (requestUserUid.equals(item.getUserUid())) {
                commentList.add(item);
            }
            if (requestUserUid.equals(item.getToUserUid())) {
                replyList.add(item);
            }
        });

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(SysConf.COMMENT_LIST, commentList);
        resultMap.put(SysConf.REPLY_LIST, replyList);
        return ResultUtil.result(SysConf.SUCCESS, resultMap);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @ApiOperation(value = "????????????????????????", notes = "????????????")
    @PostMapping("/getPraiseListByUser")
    public String getPraiseListByUser(@ApiParam(name = "currentPage", value = "????????????", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                      @ApiParam(name = "pageSize", value = "??????????????????", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        HttpServletRequest request = RequestHolder.getRequest();
        if (request.getAttribute(SysConf.USER_UID) == null || request.getAttribute(SysConf.TOKEN) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();
        QueryWrapper<Comment> queryWrappe = new QueryWrapper<>();
        queryWrappe.eq(SQLConf.USER_UID, userUid);
        queryWrappe.eq(SQLConf.TYPE, ECommentType.PRAISE);
        queryWrappe.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrappe.orderByDesc(SQLConf.CREATE_TIME);
        Page<Comment> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        IPage<Comment> pageList = commentService.page(page, queryWrappe);
        List<Comment> praiseList = pageList.getRecords();
        List<String> blogUids = new ArrayList<>();
        praiseList.forEach(item -> {
            blogUids.add(item.getBlogUid());
        });
        Map<String, Article> blogMap = new HashMap<>();
        if (blogUids.size() > 0) {
            Collection<Article> blogList = articleService.listByIds(blogUids);
            blogList.forEach(blog -> {
                // ????????????content??????
                blog.setContent("");
                blogMap.put(blog.getUid(), blog);
            });
        }

        praiseList.forEach(item -> {
            if (blogMap.get(item.getBlogUid()) != null) {
                item.setArticle(blogMap.get(item.getBlogUid()));
            }
        });
        pageList.setRecords(praiseList);
        return ResultUtil.result(SysConf.SUCCESS, pageList);
    }

    @BussinessLog(value = "????????????", behavior = EBehavior.PUBLISH_COMMENT)
    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody CommentVO commentVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        HttpServletRequest request = RequestHolder.getRequest();
        if (request.getAttribute(SysConf.USER_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.INVALID_TOKEN);
        }
        QueryWrapper<WebConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SysConf.STATUS, EStatus.ENABLE);
        WebConfig webConfig = webConfigService.getOne(queryWrapper);
        // ????????????????????????????????????
        if (SysConf.CAN_NOT_COMMENT.equals(webConfig.getOpenComment())) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.NO_COMMENTS_OPEN);
        }
        // ???????????????????????????????????????
        if (StringUtils.isNotEmpty(commentVO.getBlogUid())) {
            Article blog = articleService.getById(commentVO.getBlogUid());
            if (SysConf.CAN_NOT_COMMENT.equals(blog.getOpenComment())) {
                return ResultUtil.result(SysConf.ERROR, MessageConf.BLOG_NO_OPEN_COMMENTS);
            }
        }
        String userUid = request.getAttribute(SysConf.USER_UID).toString();
        User user = userService.getById(userUid);
        // ??????????????????????????????
        if (commentVO.getContent().length() > SysConf.ONE_ZERO_TWO_FOUR) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.COMMENT_CAN_NOT_MORE_THAN_1024);
        }
        // ??????????????????????????????
        if (user.getCommentStatus() == SysConf.ZERO) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.YOU_DONT_HAVE_PERMISSION_TO_SPEAK);
        }
        // ???????????????????????????????????????
        String jsonResult = redisUtil.get(RedisConf.USER_PUBLISH_SPAM_COMMENT_COUNT + BaseSysConf.REDIS_SEGMENTATION + userUid);
        if (!StringUtils.isEmpty(jsonResult)) {
            Integer count = Integer.valueOf(jsonResult);
            if (count >= Constants.NUM_FIVE) {
                return ResultUtil.result(SysConf.ERROR, MessageConf.PLEASE_TRY_AGAIN_IN_AN_HOUR);
            }
        }
        // ????????????????????????
        String content = commentVO.getContent();
        if (StringUtils.isCommentSpam(content)) {
            if (StringUtils.isEmpty(jsonResult)) {
                Integer count = 0;
                redisUtil.setEx(RedisConf.USER_PUBLISH_SPAM_COMMENT_COUNT + BaseSysConf.REDIS_SEGMENTATION + userUid, count.toString(), 1, TimeUnit.HOURS);
            } else {
                redisUtil.incrBy(RedisConf.USER_PUBLISH_SPAM_COMMENT_COUNT + BaseSysConf.REDIS_SEGMENTATION + userUid, 1);
            }
            return ResultUtil.result(SysConf.ERROR, MessageConf.COMMENT_IS_SPAM);
        }
        // ????????????????????????????????????????????????????????????
        if (StringUtils.isNotEmpty(commentVO.getToUserUid())) {
            User toUser = userService.getById(commentVO.getToUserUid());
            if (toUser.getStartEmailNotification() == SysConf.ONE) {
                Comment toComment = commentService.getById(commentVO.getToUid());
                if (toComment != null && StringUtils.isNotEmpty(toComment.getContent())) {
                    Map<String, String> map = new HashMap<>();
                    map.put(SysConf.EMAIL, toUser.getEmail());
                    map.put(SysConf.TEXT, commentVO.getContent());
                    map.put(SysConf.TO_TEXT, toComment.getContent());
                    map.put(SysConf.NICKNAME, user.getNickName());
                    map.put(SysConf.TO_NICKNAME, toUser.getNickName());
                    map.put(SysConf.USER_UID, toUser.getUid());
                    // ???????????????????????????
                    String commentSource = toComment.getSource();
                    String url = new String();
                    switch (commentSource) {
                        case "ABOUT": {
                            url = dataWebsiteUrl + "about";
                        }
                        break;
                        case "BLOG_INFO": {
                            url = dataWebsiteUrl + "info?blogUid=" + toComment.getBlogUid();
                        }
                        break;
                        case "MESSAGE_BOARD": {
                            url = dataWebsiteUrl + "messageBoard";
                        }
                        break;
                        default: {
                            log.error("?????????????????????");
                        }
                    }
                    map.put(SysConf.URL, url);
                    // ??????????????????
                    rabbitMqUtil.sendCommentEmail(map);
                }
            }
        }

        Comment comment = new Comment();
        comment.setSource(commentVO.getSource());
        comment.setBlogUid(commentVO.getBlogUid());
        comment.setContent(commentVO.getContent());
        comment.setToUserUid(commentVO.getToUserUid());

        // ????????????????????????????????????????????????????????????UID??????
        if (StringUtils.isNotEmpty(commentVO.getToUid())) {
            Comment toComment = commentService.getById(commentVO.getToUid());
            // ?????? toComment??????????????????
            if (toComment != null && StringUtils.isNotEmpty(toComment.getFirstCommentUid())) {
                comment.setFirstCommentUid(toComment.getFirstCommentUid());
            } else {
                // ?????????????????????????????????????????????UID
                comment.setFirstCommentUid(toComment.getUid());
            }
        } else {
            // ??????????????????????????????????????????????????? ????????????????????????????????????
            // ??????????????????????????????
            SystemConfig systemConfig = systemConfigService.getConfig();
            if (systemConfig != null && EOpenStatus.OPEN.equals(systemConfig.getStartEmailNotification())) {
                if (StringUtils.isNotEmpty(systemConfig.getEmail())) {
                    log.info("????????????????????????");
                    String sourceName = ECommentSource.valueOf(commentVO.getSource()).getName();
                    String linkText = "<a href=\" " + getUrlByCommentSource(commentVO) + "\">" + sourceName + "</a>\n";
                    String commentContent = linkText + "??????????????????: " + commentVO.getContent();
                    rabbitMqUtil.sendSimpleEmail(systemConfig.getEmail(), commentContent);
                } else {
                    log.error("????????????????????????????????????????????????");
                }
            }
        }

        comment.setUserUid(commentVO.getUserUid());
        comment.setToUid(commentVO.getToUid());
        comment.setStatus(EStatus.ENABLE);
        comment.insert();

        //????????????
        if (StringUtils.isNotEmpty(user.getAvatar())) {
            String pictureList = this.pictureFeignClient.getPicture(user.getAvatar(), SysConf.FILE_SEGMENTATION);
            if (webUtil.getPicture(pictureList).size() > 0) {
                user.setPhotoUrl(webUtil.getPicture(pictureList).get(0));
            }
        }
        comment.setUser(user);
        return ResultUtil.result(SysConf.SUCCESS, comment);
    }


    @BussinessLog(value = "????????????", behavior = EBehavior.REPORT_COMMENT)
    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/report")
    public String reportComment(HttpServletRequest request, @Validated({GetOne.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        ThrowableUtils.checkParamArgument(result);
        Comment comment = commentService.getById(commentVO.getUid());
        // ???????????????????????????
        if (comment == null || comment.getStatus() == EStatus.DISABLED) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.COMMENT_IS_NOT_EXIST);
        }

        // ???????????????????????????????????????
        if (comment.getUserUid().equals(commentVO.getUserUid())) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.CAN_NOT_REPORT_YOURSELF_COMMENTS);
        }
        // ??????????????????????????????????????????
        QueryWrapper<CommentReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.USER_UID, commentVO.getUserUid());
        queryWrapper.eq(SQLConf.REPORT_COMMENT_UID, comment.getUid());
        List<CommentReport> commentReportList = commentReportService.list(queryWrapper);
        if (commentReportList.size() > 0) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.CAN_NOT_REPEAT_REPORT_COMMENT);
        }
        CommentReport commentReport = new CommentReport();
        commentReport.setContent(commentVO.getContent());
        commentReport.setProgress(0);
        // ???VO????????????????????????uid
        commentReport.setUserUid(commentVO.getUserUid());
        commentReport.setReportCommentUid(comment.getUid());
        // ???entity???????????????????????????uid
        commentReport.setReportUserUid(comment.getUserUid());
        commentReport.setStatus(EStatus.ENABLE);
        commentReport.insert();

        return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
    }

    /**
     * ??????UID????????????
     *
     * @param request
     * @param commentVO
     * @param result
     * @return
     */
    @BussinessLog(value = "????????????", behavior = EBehavior.DELETE_COMMENT)
    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/delete")
    public String deleteBatch(HttpServletRequest request, @Validated({Delete.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        ThrowableUtils.checkParamArgument(result);
        Comment comment = commentService.getById(commentVO.getUid());
        // ?????????????????????????????????
        if (!comment.getUserUid().equals(commentVO.getUserUid())) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.DATA_NO_PRIVILEGE);
        }
        comment.setStatus(EStatus.DISABLED);
        comment.updateById();

        // ??????????????????????????????????????????
        // ?????????????????????????????? ??????????????????????????????????????????List???????????????????????????????????????????????????????????????
        List<Comment> commentList = new ArrayList<>(Constants.NUM_ONE);
        commentList.add(comment);

        // ?????????????????????????????????????????????
        String firstCommentUid = "";
        if (StringUtils.isNotEmpty(comment.getFirstCommentUid())) {
            // ?????????????????????
            firstCommentUid = comment.getFirstCommentUid();
        } else {
            // ????????????????????????
            firstCommentUid = comment.getUid();
        }

        // ????????????????????????????????????????????????
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SQLConf.FIRST_COMMENT_UID, firstCommentUid);
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        List<Comment> toCommentList = commentService.list(queryWrapper);
        List<Comment> resultList = new ArrayList<>();
        this.getToCommentList(comment, toCommentList, resultList);
        // ??????????????????????????????
        if (resultList.size() > 0) {
            resultList.forEach(item -> {
                item.setStatus(EStatus.DISABLED);
                item.setUpdateTime(new Date());
            });
            commentService.updateBatchById(resultList);
        }

        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
    }

    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    @GetMapping("/closeEmailNotification/{userUid}")
    public String bindUserEmail(@PathVariable("userUid") String userUid) {

        User user = userService.getById(userUid);
        if (user == null) {
            ResultUtil.result(SysConf.ERROR, MessageConf.OPERATION_FAIL);
        }
        user.setStartEmailNotification(0);
        user.updateById();

        // ??????user????????????token????????????redis????????????
        if (StringUtils.isNotEmpty(user.getValidCode())) {
            String accessToken = user.getValidCode();
            String userInfo = redisUtil.get(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken);
            if (StringUtils.isNotEmpty(userInfo)) {
                Map<String, Object> map = JsonUtils.jsonToMap(userInfo);
                // ??????????????????
                map.put(SysConf.START_EMAIL_NOTIFICATION, 0);
                redisUtil.setEx(RedisConf.USER_TOKEN + Constants.SYMBOL_COLON + accessToken, JsonUtils.objectToJson(map), userTokenSurvivalTime, TimeUnit.HOURS);
            }
        }

        return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
    }


    /**
     * ????????????????????????
     *
     * @param list
     * @param toCommentListMap
     * @return
     */
    private List<Comment> getCommentReplys(List<Comment> list, Map<String, List<Comment>> toCommentListMap) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        } else {
            list.forEach(item -> {
                String commentUid = item.getUid();
                List<Comment> replyCommentList = toCommentListMap.get(commentUid);
                List<Comment> replyComments = getCommentReplys(replyCommentList, toCommentListMap);
                item.setReplyList(replyComments);
            });
            return list;
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @return
     */
    private void getToCommentList(Comment comment, List<Comment> commentList, List<Comment> resultList) {
        if (comment == null) {
            return;
        }
        String commentUid = comment.getUid();
        for (Comment item : commentList) {
            if (commentUid.equals(item.getToUid())) {
                resultList.add(item);
                // ???????????????????????????
                getToCommentList(item, commentList, resultList);
            }
        }
    }

    /**
     * ??????????????????????????????????????????
     * @param commentVO
     * @return
     */
    private String getUrlByCommentSource(CommentVO commentVO) {
        String linkUrl = new String();
        String commentSource = commentVO.getSource();
        switch (commentSource) {
            case "ABOUT": {
                linkUrl = dataWebsiteUrl + "about";
            }
            break;
            case "BLOG_INFO": {
                linkUrl = dataWebsiteUrl + "info?blogUid=" + commentVO.getBlogUid();
            }
            break;
            case "MESSAGE_BOARD": {
                linkUrl = dataWebsiteUrl + "messageBoard";
            }
            break;
            default: {
                linkUrl = dataWebsiteUrl;
                log.error("?????????????????????");
            }
        }
        return linkUrl;
    }

}

