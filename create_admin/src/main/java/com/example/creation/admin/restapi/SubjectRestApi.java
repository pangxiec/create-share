package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.SubjectService;
import com.example.creation.xo.vo.SubjectVO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 专题表 RestApi
 *
 */
@Api(value = "专题相关接口", tags = {"专题相关接口"})
@RestController
@RequestMapping("/subject")
@Slf4j
public class SubjectRestApi {

    @Resource
    private SubjectService subjectService;

    @AuthorityVerify
    @ApiOperation(value = "获取专题列表", notes = "获取专题列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody SubjectVO subjectVO, BindingResult result) {

        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(subjectService.getPageList(subjectVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加专题")
    @ApiOperation(value = "增加专题", notes = "增加专题", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody SubjectVO subjectVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return subjectService.addSubject(subjectVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑专题")
    @ApiOperation(value = "编辑专题", notes = "编辑专题", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody SubjectVO subjectVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return subjectService.editSubject(subjectVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "批量删除专题")
    @ApiOperation(value = "批量删除专题", notes = "批量删除专题", response = String.class)
    @PostMapping("/deleteBatch")
    public String delete(@Validated({Delete.class}) @RequestBody List<SubjectVO> subjectVOList, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return subjectService.deleteBatchSubject(subjectVOList);
    }
}

