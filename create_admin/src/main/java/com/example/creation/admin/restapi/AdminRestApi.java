package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.xo.service.AdminService;
import com.example.creation.xo.vo.AdminVO;
import com.example.creation.base.exception.ThrowableUtils;
import com.example.creation.base.validator.group.GetList;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xmy
 * @date 2021/4/5 16:50
 */
@RestController
@RequestMapping("/admin")
@Api(value = "管理员相关接口", tags = {"管理员相关接口"})
@Slf4j
public class AdminRestApi {

    @Resource
    private AdminService adminService;

    @AuthorityVerify
    @ApiOperation(value = "获取管理员列表", notes = "获取管理员列表")
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody AdminVO adminVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return adminService.getList(adminVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "重置用户密码")
    @ApiOperation(value = "重置用户密码", notes = "重置用户密码")
    @PostMapping("/restPwd")
    public String restPwd(@Validated({Update.class}) @RequestBody AdminVO adminVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return adminService.resetPwd(adminVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "新增管理员")
    @ApiOperation(value = "新增管理员", notes = "新增管理员")
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody AdminVO adminVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return adminService.addAdmin(adminVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "更新管理员")
    @ApiOperation(value = "更新管理员", notes = "更新管理员")
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody AdminVO adminVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return adminService.editAdmin(adminVO);
    }


    @AuthorityVerify
    @OperationLogger(value = "批量删除管理员")
    @ApiOperation(value = "批量删除管理员", notes = "批量删除管理员")
    @PostMapping("/delete")
    public String delete(@ApiParam(name = "adminUids", value = "管理员uid集合", required = true) @RequestParam(name = "adminUids", required = true) List<String> adminUids) {
        return adminService.deleteBatchAdmin(adminUids);
    }

    @AuthorityVerify
    @ApiOperation(value = "获取在线管理员列表", notes = "获取在线管理员列表", response = String.class)
    @PostMapping(value = "/getOnlineAdminList")
    public String getOnlineAdminList(@Validated({GetList.class}) @RequestBody AdminVO adminVO, BindingResult result) {
        ThrowableUtils.checkParamArgument(result);
        return adminService.getOnlineAdminList(adminVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "强退用户")
    @ApiOperation(value = "强退用户", notes = "强退用户", response = String.class)
    @PostMapping(value = "/forceLogout")
    public String forceLogout(@ApiParam(name = "tokenUidList", value = "tokenList", required = false) @RequestBody List<String> tokenUidList) {
        return adminService.forceLogout(tokenUidList);
    }
}
