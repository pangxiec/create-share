package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.CategoryMenuService;
import com.example.creation.xo.vo.CategoryMenuVO;
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
 * 菜单表 RestApi
 *
 */

@RestController
@RequestMapping("/categoryMenu")
@Api(value = "菜单信息相关接口", tags = {"菜单信息相关接口"})
@Slf4j
public class CategoryMenuRestApi {

    @Resource
    CategoryMenuService categoryMenuService;

    @AuthorityVerify
    @ApiOperation(value = "获取菜单列表", notes = "获取菜单列表", response = String.class)
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public String getList(@Validated({GetList.class}) @RequestBody CategoryMenuVO categoryMenuVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(categoryMenuService.getPageList(categoryMenuVO));
    }

    @ApiOperation(value = "获取所有菜单列表", notes = "获取所有列表", response = String.class)
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword) {
        return ResultUtil.successWithData(categoryMenuService.getAllList(keyword));
    }

    @ApiOperation(value = "获取所有二级菜单-按钮列表", notes = "获取所有二级菜单-按钮列表", response = String.class)
    @RequestMapping(value = "/getButtonAll", method = RequestMethod.GET)
    public String getButtonAll(@RequestParam(value = "keyword", required = false) String keyword) {

        return ResultUtil.successWithData(categoryMenuService.getButtonAllList(keyword));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加菜单")
    @ApiOperation(value = "增加菜单", notes = "增加菜单", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody CategoryMenuVO categoryMenuVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return categoryMenuService.addCategoryMenu(categoryMenuVO);
    }

    @AuthorityVerify
    @ApiOperation(value = "编辑菜单", notes = "编辑菜单", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody CategoryMenuVO categoryMenuVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return categoryMenuService.editCategoryMenu(categoryMenuVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除菜单")
    @ApiOperation(value = "删除菜单", notes = "删除菜单", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody CategoryMenuVO categoryMenuVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return categoryMenuService.deleteCategoryMenu(categoryMenuVO);
    }

    /**
     * 如果是一级菜单，直接置顶在最前面，二级菜单，就在一级菜单内置顶
     *
     * @author xzx19950624@qq.com
     * @date 2018年11月29日上午9:22:59
     */
    @AuthorityVerify
    @OperationLogger(value = "置顶菜单")
    @ApiOperation(value = "置顶菜单", notes = "置顶菜单", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody CategoryMenuVO categoryMenuVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return categoryMenuService.stickCategoryMenu(categoryMenuVO);
    }
}

