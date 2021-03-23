package com.example.creation.admin.restapi;


import com.example.creation.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.example.creation.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.example.creation.admin.annotion.OperationLogger.OperationLogger;
import com.example.creation.admin.global.SysConf;
import com.example.creation.commons.entity.PictureSort;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.PictureSortService;
import com.example.creation.xo.vo.PictureSortVO;
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

/**
 * 图片分类表 RestApi
 *
 */
@Api(value = "图片分类相关接口", tags = {"图片分类相关接口"})
@RestController
@RequestMapping("/pictureSort")
@Slf4j
public class PictureSortRestApi {

    @Resource
    private PictureSortService pictureSortService;

    @AuthorityVerify
    @ApiOperation(value = "获取图片分类列表", notes = "获取图片分类列表", response = String.class)
    @PostMapping(value = "/getList")
    public String getList(@Validated({GetList.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("获取图片分类列表: {}", pictureSortVO);
        return ResultUtil.result(SysConf.SUCCESS, pictureSortService.getPageList(pictureSortVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加图片分类")
    @ApiOperation(value = "增加图片分类", notes = "增加图片分类", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("增加图片分类: {}", pictureSortVO);
        return pictureSortService.addPictureSort(pictureSortVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑图片分类")
    @ApiOperation(value = "编辑图片分类", notes = "编辑图片分类", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑图片分类: {}", pictureSortVO);
        return pictureSortService.editPictureSort(pictureSortVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除图片分类")
    @ApiOperation(value = "删除图片分类", notes = "删除图片分类", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("删除图片分类: {}", pictureSortVO);
        return pictureSortService.deletePictureSort(pictureSortVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "置顶分类")
    @ApiOperation(value = "置顶分类", notes = "置顶分类", response = String.class)
    @PostMapping("/stick")
    public String stick(@Validated({Delete.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("置顶图片分类: {}", pictureSortVO);
        return pictureSortService.stickPictureSort(pictureSortVO);
    }

    @OperationLogger(value = "通过Uid获取分类")
    @ApiOperation(value = "通过Uid获取分类", notes = "通过Uid获取分类", response = String.class)
    @PostMapping("/getPictureSortByUid")
    public String getPictureSortByUid(@Validated({Delete.class}) @RequestBody PictureSortVO pictureSortVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        log.info("通过Uid获取分类: {}", pictureSort);
        return ResultUtil.successWithData(pictureSort);
    }
}

