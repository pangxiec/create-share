package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * <p>
 * 文章分类表
 * </p>
 *
 * @author xmy
 * @date 2021/3/17 10:57
 */
@Data
@TableName("article_sort")
public class ArticleSort extends SuperEntity<ArticleSort> {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名
     */
    private String sortName;

    /**
     * 分类介绍
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String content;

    /**
     * 点击数
     */
    private Integer clickCount;

    /**
     * 排序字段，数值越大，越靠前
     */
    private Integer sort;
}
