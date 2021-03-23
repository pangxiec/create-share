package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * 存储信息实体类
 *
 * @author xmy
 * @date 2021/2/6 17:27
 */
@TableName("storage")
@Data
public class Storage extends SuperEntity<Storage> {

    /**
     * 管理员UID
     */
    private String adminUid;

    /**
     * 当前网盘容量
     */
    private long storageSize;

    /**
     * 最大网盘容量
     */
    private long maxStorageSize;
}
