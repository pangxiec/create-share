package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 学习视频表
 * </p>
 *
 * @author xmy
 * @date 2021/2/6 17:27
 */
@Data
@TableName("study_video")
public class StudyVideo extends SuperEntity<StudyVideo> {

    private static final long serialVersionUID = 1L;


    /**
     * 视频名称
     */
    private String name;

    /**
     * 视频简介
     */
    private String summary;

    /**
     * 视频内容介绍
     */
    private String content;

    /**
     * 百度云完整路径
     */
    private String baiduPath;

    /**
     * 视频封面图片UID
     */
    private String fileUid;

    /**
     * 资源分类UID
     */
    private String resourceSortUid;

    /**
     * 点击数
     */
    private Integer clickCount;

    /**
     * 学习视频标题图
     */
    @TableField(exist = false)
    private List<String> photoList;

    /**
     * 资源分类
     */
    @TableField(exist = false)
    private ResourceSort resourceSort;
}
