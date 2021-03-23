//package com.example.creation.search.pojo;
//
//import lombok.Data;
//import org.apache.solr.client.solrj.beans.Field;
//
//import java.util.Date;
//
///**
// * 用于建立solr索引实体类
// *
// * @author limboy
// * @create 2018-09-29 16:12
// */
//@Data
//public class SolrIndex {
//
//    /**
//     * 文章uid
//     */
//    @Field("id")
//    private String id;
//
//    @Field("oid")
//    private Integer oid;
//
//    @Field("blog_type")
//    private String type;
//
//    /**
//     * 图片Uid
//     */
//    @Field("file_uid")
//    private String fileUid;
//
//    /**
//     * 文章标题
//     */
//    @Field("blog_title")
//    private String title;
//
//    /**
//     * 文章简介
//     */
//    @Field("blog_summary")
//    private String summary;
//
//    /**
//     * 文章简介
//     */
//    @Field("blog_content")
//    private String blogContent;
//
//    /**
//     * 标签名
//     */
//    @Field("blog_tag_name")
//    private String blogTagName;
//
//    /**
//     * 文章分类名
//     */
//    @Field("blog_sort_name")
//    private String blogSortName;
//
//    /**
//     * 文章标签UID
//     */
//    @Field("blog_tag_uid")
//    private String blogTagUid;
//
//    /**
//     * 文章分类UID
//     */
//    @Field("blog_sort_uid")
//    private String blogSortUid;
//
//    /**
//     * 如果原创，作者为管理员名
//     */
//    @Field("blog_author")
//    private String author;
//
//    /**
//     * 文章创建时间
//     */
//    @Field("create_time")
//    private Date createTime;
//
//    /**
//     * 标题图
//     */
//    @Field("photo_url")
//    private String photoUrl;
//}
