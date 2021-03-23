package com.example.creation.admin.restapi;

import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.WebNavbarService;
import com.example.creation.xo.vo.WebNavbarVO;
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

import javax.annotation.Resource;

/**
 * 门户导航栏管理
 *
 */
@RestController
@RequestMapping("/webNavbar")
@Api(value = "门户导航栏管理", tags = {"门户导航栏相关接口"})
@Slf4j
public class WebNavbarRestApi {

    @Resource
    private WebNavbarService webNavbarService;

    @AuthorityVerify
    @ApiOperation(value = "获取门户导航栏列表", notes = "获取门户导航栏列表", response = String.class)
    @GetMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(webNavbarService.getPageList(webNavbarVO));
    }

    @ApiOperation(value = "获取门户导航栏所有列表", notes = "获取门户导航栏所有列表", response = String.class)
    @GetMapping("/getAllList")
    public String getAllList() {
        return ResultUtil.successWithData(webNavbarService.getAllList());
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加门户导航栏")
    @ApiOperation(value = "增加门户导航栏", notes = "增加门户导航栏", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("增加门户导航栏");
        return webNavbarService.addWebNavbar(webNavbarVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑门户导航栏")
    @ApiOperation(value = "编辑门户导航栏", notes = "编辑门户导航栏", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑门户导航栏");
        return webNavbarService.editWebNavbar(webNavbarVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除门户导航栏")
    @ApiOperation(value = "删除门户导航栏", notes = "删除门户导航栏", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        log.info("批量删除门户导航栏");
        return webNavbarService.deleteWebNavbar(webNavbarVO);
    }
}

