package com.example.creation.picture.vo;

import com.example.creation.base.vo.BaseVO;
import lombok.Data;

/**
 * CommentVO
 *
 */
@Data
public class StorageVO extends BaseVO<StorageVO> {

    /**
     * 管理员UID
     */
    private String adminUid;

    /**
     * 存储大小
     */
    private long storagesize;
}
