package com.example.creation.commons.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.creation.base.entity.SuperEntity;
import lombok.Data;

import java.util.List;

/**
 * 文章表
 *
 * @author xmy
 * @date 2021/3/16 11:24
 */
@Data
@TableName("article")
public class Article extends SuperEntity<Article> {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一oid【自动递增】
     */
    private Integer oid;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章简介
     * updateStrategy = FieldStrategy.IGNORED ：表示更新时候忽略非空判断
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签uid
     */
    private String tagUid;

    /**
     * 文章分类UID
     */
    private String articleSortUid;

    /**
     * 文章点击数
     */
    private Integer clickCount;

    /**
     * 文章收藏数
     */
    private Integer collectCount;

    /**
     * 标题图片UID
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String fileUid;

    /**
     * 文章封面图片
     */
    private String coverUrl;

    /**
     * 管理员UID
     */
    private String adminUid;

    /**
     * 是否发布
     */
    private String isPublish;

    /**
     * 是否原创
     */
    private String isOriginal;

    /**
     * 如果原创，作者为管理员名
     */
    private String author;

    /**
     * 文章出处
     */
    private String articlesPart;

    /**
     * 推荐级别，用于首页推荐
     * 0：正常
     * 1：一级推荐(轮播图)
     * 2：二级推荐(top)
     * 3：三级推荐 ()
     * 4：四级 推荐 (特别推荐)
     */
    private Integer level;

    /**
     * 排序字段，数值越大，越靠前
     */
    private Integer sort;

    /**
     * 是否开启评论(0:否， 1:是)
     */
    private String openComment;

    /**
     * 文章类型【0 文章， 1：推广】
     */
    private String type;

    /**
     * 外链【如果是推广，那么将跳转到外链】
     */
    private String outsideLink;


    // 以下字段不存入数据库，封装为了方便使用

    /**
     * 标签,一篇文章对应多个标签
     */
    @TableField(exist = false)
    private List<Tag> tagList;

    /**
     * 标题图
     */
    @TableField(exist = false)
    private List<String> photoList;

    /**
     * 文章分类
     */
    @TableField(exist = false)
    private ArticleSort articleSort;

    /**
     * 文章分类名
     */
    @TableField(exist = false)
    private String blogSortName;

    /**
     * 文章标题图
     */
    @TableField(exist = false)
    private String photoUrl;

    /**
     * 点赞数
     */
    @TableField(exist = false)
    private Integer praiseCount;

    /**
     * 版权申明
     */
    @TableField(exist = false)
    private String copyright;
}
