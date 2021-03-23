package com.example.creation.base.vo;

import com.example.creation.base.validator.annotion.IdValid;
import com.example.creation.base.validator.group.Delete;
import com.example.creation.base.validator.group.Update;
import lombok.Data;

/**
 * BaseVO   view object 表现层 基类对象
 *
 * @author xmy
 * @date 2021/3/12 14:09
 */
@Data
public class BaseVO<T> extends PageInfo<T> {

    /**
     * 唯一UID
     */
    @IdValid(groups = {Update.class, Delete.class})
    private String uid;

    private Integer status;
}
