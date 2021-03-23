package com.example.creation.xo.vo;

import com.example.creation.base.validator.annotion.NotBlank;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import com.example.creation.base.vo.BaseVO;
import lombok.Data;

/**
 * @author xmy
 * @date 2021/3/17 11:34
 */
@Data
public class ArticleSortVO extends BaseVO<ArticleSortVO> {

    /**
     * 分类名
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String sortName;

    /**
     * 分类介绍
     */
    private String content;

    /**
     * 排序字段
     */
    private Integer sort;


    /**
     * OrderBy排序字段（desc: 降序）
     */
    private String orderByDescColumn;

    /**
     * OrderBy排序字段（asc: 升序）
     */
    private String orderByAscColumn;

    /**
     * 无参构造方法
     */
    ArticleSortVO() {

    }

}
