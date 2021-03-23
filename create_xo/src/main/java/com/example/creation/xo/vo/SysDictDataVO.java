package com.example.creation.xo.vo;

import com.example.creation.base.validator.annotion.IntegerNotNull;
import com.example.creation.base.validator.annotion.NotBlank;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import com.example.creation.base.vo.BaseVO;
import lombok.Data;

/**
 * <p>
 * SysDictTypeVO
 * </p>
 *
 */
@Data
public class SysDictDataVO extends BaseVO<SysDictDataVO> {


    /**
     * 自增键 oid
     */
    private Long oid;

    /**
     * 字典标签
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String dictLabel;

    /**
     * 字典键值
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String dictValue;

    /**
     * 字典类型UID
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String dictTypeUid;

    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（1是 0否）,默认为0
     */
    @IntegerNotNull(groups = {Insert.class, Update.class})
    private Integer isDefault;

    /**
     * 是否发布  1：是，0:否，默认为0
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String isPublish;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序字段
     */
    @IntegerNotNull(groups = {Insert.class, Update.class})
    private Integer sort;

}
