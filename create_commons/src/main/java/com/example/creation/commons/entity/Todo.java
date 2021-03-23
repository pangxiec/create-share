package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

/**
 * <p>
 * 代办事项表
 * </p>
 *
 * @author xmy
 * @date 2021/2/6 17:27
 */
@Data
@TableName("todo")
public class Todo extends SuperEntity<Todo> {

    private static final long serialVersionUID = 1L;

    /**
     * 内容
     */
    private String text;

    /**
     * 管理员UID
     */
    private String adminUid;

    /**
     * 表示事项是否完成
     */
    private Boolean done;
}
