package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.SubjectItemService;
import com.example.creation.xo.vo.SubjectItemVO;
import com.example.creation.base.exception.ThrowableUtils;
import com.example.creation.base.validator.group.GetList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@Api(value = "专题Item相关接口", tags = {"专题Item相关接口"})
@RestController
@RequestMapping("/subjectItem")
@Slf4j
public class SubjectItemRestApi {

    @Resource
    private SubjectItemService subjectItemService;

    @AuthorityVerify
    @ApiOperation(value = "获取专题Item列表", notes = "获取专题Item列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody SubjectItemVO subjectItemVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(subjectItemService.getPageList(subjectItemVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加专题Item")
    @ApiOperation(value = "增加专题Item", notes = "增加专题Item", response = String.class)
    @PostMapping("/add")
    public String add(@RequestBody List<SubjectItemVO> subjectItemVOList, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return subjectItemService.addSubjectItemList(subjectItemVOList);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑专题Item")
    @ApiOperation(value = "编辑专题Item", notes = "编辑专题Item", response = String.class)
    @PostMapping("/edit")
    public String edit(@RequestBody List<SubjectItemVO> subjectItemVOList, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return subjectItemService.editSubjectItemList(subjectItemVOList);
    }

    @AuthorityVerify
    @OperationLogger(value = "批量删除专题Item")
    @ApiOperation(value = "批量删除专题Item", notes = "批量删除专题Item", response = String.class)
    @PostMapping("/deleteBatch")
    public String delete(@RequestBody List<SubjectItemVO> subjectItemVOList, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return subjectItemService.deleteBatchSubjectItem(subjectItemVOList);
    }

    @AuthorityVerify
    @OperationLogger(value = "通过创建时间排序专题列表")
    @ApiOperation(value = "通过创建时间排序专题列表", notes = "通过创建时间排序专题列表", response = String.class)
    @PostMapping("/sortByCreateTime")
    public String sortByCreateTime(@ApiParam(name = "subjectUid", value = "专题uid") @RequestParam(name = "subjectUid", required = true) String subjectUid,
                                   @ApiParam(name = "isDesc", value = "是否从大到小排列") @RequestParam(name = "isDesc", required = false, defaultValue = "false") Boolean isDesc) {
        log.info("通过点击量排序文章分类");
        return subjectItemService.sortByCreateTime(subjectUid, isDesc);
    }
}

