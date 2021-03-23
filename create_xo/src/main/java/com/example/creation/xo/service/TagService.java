package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.Tag;
import com.example.creation.xo.vo.TagVO;
import com.example.creation.base.service.SuperService;

import java.util.List;

/**
 * 标签表 服务类
 *
 *
 * @date 2018-09-08
 */
public interface TagService extends SuperService<Tag> {
    /**
     * 获取文章标签列表
     *
     * @param tagVO
     * @return
     */
    public IPage<Tag> getPageList(TagVO tagVO);

    /**
     * 获取全部文章标签列表
     *
     * @return
     */
    public List<Tag> getList();

    /**
     * 新增文章标签
     *
     * @param tagVO
     */
    public String addTag(TagVO tagVO);

    /**
     * 编辑文章标签
     *
     * @param tagVO
     */
    public String editTag(TagVO tagVO);

    /**
     * 批量删除文章标签
     *
     * @param tagVOList
     */
    public String deleteBatchTag(List<TagVO> tagVOList);

    /**
     * 置顶文章标签
     *
     * @param tagVO
     */
    public String stickTag(TagVO tagVO);

    /**
     * 通过点击量排序文章
     *
     * @return
     */
    public String tagSortByClickCount();

    /**
     * 通过引用量排序文章
     *
     * @return
     */
    public String tagSortByCite();

    /**
     * 获取热门标签
     *
     * @return
     */
    public List<Tag> getHotTag(Integer hotTagCount);

    /**
     * 获取一个排序最高的标签
     *
     * @return
     */
    public Tag getTopTag();
}
