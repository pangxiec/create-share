package com.example.creation.xo.vo;

import com.example.creation.commons.entity.ArticleSort;
import com.example.creation.commons.entity.Tag;
import com.example.creation.base.validator.annotion.IntegerNotNull;
import com.example.creation.base.validator.annotion.NotBlank;
import com.example.creation.base.validator.group.Insert;
import com.example.creation.base.validator.group.Update;
import com.example.creation.base.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xmy
 * @date 2021/3/12 15:24
 */
@Data
public class ArticleVO extends BaseVO<ArticleVO> {

    /**
     * 文章标题
     */
    @NotBlank(groups = {Insert.class, Update.class})
    @ApiModelProperty(value = "文章标题")
    private String title;

    /**
     * 文章简介
     */
    private String summary;

    /**
     * 文章内容
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String content;

    /**
     * 标签uid
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String tagUid;
    /**
     * 文章分类UID
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String articleSortUid;

    /**
     * 标题图片UID
     */
    private String fileUid;


    /**
     * 管理员UID
     */
    private String adminUid;
    /**
     * 是否发布
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String isPublish;
    /**
     * 是否原创
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String isOriginal;
    /**
     * 如果原创，作者为管理员名
     */
    @NotBlank(groups = {Update.class})
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
    @IntegerNotNull(groups = {Insert.class, Update.class})
    private Integer level;

    /**
     * 类型【0 文章， 1：推广】
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String type;

    /**
     * 外链【如果是推广，那么将跳转到外链】
     */
    private String outsideLink;

    /**
     * 标签,一篇文章对应多个标签
     */
    private List<Tag> tagList;

    // 以下字段不存入数据库，封装为了方便使用
    /**
     * 标题图
     */
    private List<String> photoList;
    /**
     * 文章分类
     */
    private ArticleSort articleSort;
    /**
     * 点赞数
     */
    private Integer praiseCount;
    /**
     * 版权申明
     */
    private String copyright;

    /**
     * 文章等级关键字，仅用于getList
     */
    private String levelKeyword;

    /**
     * 使用Sort字段进行排序 （0：不使用， 1：使用），默认为0
     */
    private Integer useSort;

    /**
     * 排序字段，数值越大，越靠前
     */
    private Integer sort;

    /**
     * 是否开启评论(0:否， 1:是)
     */
    private String openComment;

    /**
     * OrderBy排序字段（desc: 降序）
     */
    private String orderByDescColumn;

    /**
     * OrderBy排序字段（asc: 升序）
     */
    private String orderByAscColumn;

    /**
     * 无参构造方法，初始化默认值
     */
    ArticleVO() {
        this.level = 0;
        this.useSort = 0;
    }
}
