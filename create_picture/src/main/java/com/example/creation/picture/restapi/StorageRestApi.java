package com.example.creation.picture.restapi;

import com.example.creation.commons.entity.NetworkDisk;
import com.example.creation.commons.entity.Storage;
import com.example.creation.picture.service.StorageService;
import com.example.creation.utils.FileUtils;
import com.example.creation.utils.ResultUtil;
import com.example.creation.base.holder.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 存储控制类
 *
 */
@RestController
@RequestMapping("/storage")
@Api(value = "存储服务相关接口", tags = {"存储服务相关接口"})
public class StorageRestApi {

    @Value(value = "${file.upload.path}")
    String path;
    @Resource
    private StorageService storageService;

    /**
     * 初始化容量大小
     *
     * @return
     */
    @PostMapping(value = "/initStorageSize")
    public String initStorageSize(@ApiParam(name = "adminUid", value = "管理员uid") @RequestParam("adminUid") String adminUid,
                                  @ApiParam(name = "maxStorageSize", value = "最大网盘容量 ") @RequestParam(value = "maxStorageSize", defaultValue = "0") Long maxStorageSize) {
        return storageService.initStorageSize(adminUid, maxStorageSize);
    }

    /**
     * 编辑容量大小
     *
     * @return
     */
    @PostMapping(value = "/editStorageSize")
    public String editStorageSize(@ApiParam(name = "adminUid", value = "管理员uid") @RequestParam("adminUid") String adminUid,
                                  @ApiParam(name = "maxStorageSize", value = "最大网盘容量 ") @RequestParam(value = "maxStorageSize", defaultValue = "0") Long maxStorageSize) {
        return storageService.editStorageSize(adminUid, maxStorageSize);
    }

    /**
     * 通过管理员uid，获取存储信息
     *
     * @return
     */
    @RequestMapping(value = "/getStorageByAdminUid", method = RequestMethod.GET)
    public String getStorageByAdminUid(@RequestParam("adminUidList") List<String> adminUidList) {
        List<Storage> storageList = storageService.getStorageByAdminUid(adminUidList);
        return ResultUtil.successWithData(storageList);
    }


    /**
     * 上传文件
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(HttpServletRequest request, NetworkDisk networkDisk) {
        RequestHolder.checkLogin();
        // 获取文件
        List<MultipartFile> fileDatas = FileUtils.getMultipartFileList(request);
        return storageService.uploadFile(networkDisk, fileDatas);
    }

    /**
     * 查询当前用户存储信息
     *
     * @return
     */
    @RequestMapping(value = "/getStorage", method = RequestMethod.GET)
    public String getStorage() {
        RequestHolder.checkLogin();
        Storage storage = storageService.getStorageByAdmin();
        return ResultUtil.successWithData(storage);
    }
}
