package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * <p>
 * 专题Item表
 * </p>
 *
 * @author xmy
 * @date 2021/2/6 17:28
 */
@Data
@TableName("subject_item")
public class SubjectItem extends SuperEntity<SubjectItem> {

    private static final long serialVersionUID = 1L;

    /**
     * 专题UID
     */
    private String subjectUid;
    /**
     * 文章uid
     */
    private String blogUid;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

    /**
     * 文章
     */
    @TableField(exist = false)
    private Article article;

}
