package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;


/**
 * <p>
 * 字典类型表
 * </p>
 *
 * @author xmy
 * @date 2021/2/6 17:31
 */
@Data
@TableName("sys_dict_type")
public class SysDictType extends SuperEntity<SysDictType> {

    /**
     * 自增键 oid
     */
    private Long oid;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 是否发布  1：是，0:否，默认为0
     */
    private String isPublish;

    /**
     * 创建人UID
     */
    private String createByUid;

    /**
     * 最后更新人UID
     */
    private String updateByUid;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序字段
     */
    private Integer sort;

}
