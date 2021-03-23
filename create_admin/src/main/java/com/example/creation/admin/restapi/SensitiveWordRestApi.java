package com.example.creation.admin.restapi;

import com.example.creation.commons.entity.SensitiveWord;
import com.example.creation.utils.ResultUtil;
import com.example.creation.xo.service.SensitiveWordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xmy
 * @date 2021/3/16 17:04
 */
@RestController
@RequestMapping("/blogWord")
@Api(value = "否词相关接口", tags = {"否词相关接口"})
@Slf4j
public class SensitiveWordRestApi {

    @Resource
    private SensitiveWordService sensitiveWordService;

    @ApiOperation(value = "展示否词")
    @GetMapping("/selectWord")
    public String selectNegativeWord(){
        List<SensitiveWord> list = sensitiveWordService.list();
        return ResultUtil.successWithData(list);
    }


}
