package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * <p>
 * 收藏表
 * </p>
 *
 * @author xmy
 * @date 2021/3/19 12:13
 */
@Data
@TableName("collect")
public class Collect extends SuperEntity<Collect> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户的uid
     */
    private String userUid;

    /**
     * 文章的uid
     */
    private String blogUid;
}
