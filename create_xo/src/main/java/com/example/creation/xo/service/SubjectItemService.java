package com.example.creation.xo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.creation.commons.entity.SubjectItem;
import com.example.creation.xo.vo.SubjectItemVO;
import com.example.creation.base.service.SuperService;

import java.util.List;

/**
 * 专题item表 服务类
 *
 */
public interface SubjectItemService extends SuperService<SubjectItem> {

    /**
     * 获取专题item列表
     *
     * @param subjectItemVO
     * @return
     */
    IPage<SubjectItem> getPageList(SubjectItemVO subjectItemVO);

    /**
     * 批量新增专题
     *
     * @param subjectItemVOList
     */
    String addSubjectItemList(List<SubjectItemVO> subjectItemVOList);

    /**
     * 编辑专题item
     *
     * @param subjectItemVOList
     */
    String editSubjectItemList(List<SubjectItemVO> subjectItemVOList);

    /**
     * 批量删除专题item
     *
     * @param subjectItemVOList
     */
    String deleteBatchSubjectItem(List<SubjectItemVO> subjectItemVOList);

    /**
     * 通过文章uid批量删除专题item
     *
     * @param blogUid
     * @return
     */
    String deleteBatchSubjectItemByBlogUid(List<String> blogUid);

    /**
     * 通过创建时间排序专题列表
     * @param isDesc
     * @return
     */
    String sortByCreateTime(String subjectUid, Boolean isDesc);

}
