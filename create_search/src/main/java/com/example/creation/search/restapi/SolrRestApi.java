//package com.example.creation.search.restapi;
//
//import com.example.creation.commons.entity.Blog;
//import com.example.creation.commons.feign.WebFeignClient;
//import com.example.creation.search.global.MessageConf;
//import com.example.creation.search.global.SysConf;
//import com.example.creation.search.service.SolrSearchService;
//import com.example.creation.utils.ResultUtil;
//import com.example.creation.utils.StringUtils;
//import com.example.creation.utils.WebUtils;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.text.ParseException;
//import java.util.List;
//import java.util.Map;
//
///**
// * Solr搜索相关接口
// *
// */
//@RestController
//@RequestMapping("/search")
//@Api(value = "Solr相关接口", tags = {"Solr相关接口"})
//@Slf4j
//public class SolrRestApi {
//
//    @Resource
//    private SolrSearchService solrSearchService;
//
//    @Resource
//    private WebFeignClient webFeignClient;
//
//    @Value(value = "${spring.data.solr.core}")
//    private String collection;
//
//    @ApiOperation(value = "通过Solr搜索文章", notes = "通过Solr搜索文章", response = String.class)
//    @GetMapping("/solrSearchBlog")
//    public String solrSearchBlog(HttpServletRequest request,
//                                 @RequestParam(required = false) String keywords,
//                                 @RequestParam(name = "currentPage", required = false, defaultValue = "1") Integer currentPage,
//                                 @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
//
//        if (StringUtils.isEmpty(keywords)) {
//            return ResultUtil.result(SysConf.ERROR, MessageConf.KEYWORD_IS_NOT_EMPTY);
//        }
//        log.error("使用Solr搜索：keywords:" + keywords);
//        Map<String, Object> map = solrSearchService.search(collection, keywords, currentPage, pageSize);
//        return ResultUtil.result(SysConf.SUCCESS, map);
//    }
//
//    @ApiOperation(value = "通过文章Uid添加Solr索引", notes = "通过文章Uid添加Solr索引", response = String.class)
//    @PostMapping("/addSolrIndexByUid")
//    public String addSolrIndexByUid(@RequestParam(required = true) String uid) {
//        log.info("通过文章Uid添加Solr索引");
//        String result = webFeignClient.getBlogByUid(uid);
//        Blog blog = WebUtils.getData(result, Blog.class);
//        if (blog == null) {
//            return ResultUtil.result(SysConf.ERROR, MessageConf.INSERT_SUCCESS);
//        }
//        solrSearchService.addIndex(collection, blog);
//        return ResultUtil.result(SysConf.SUCCESS, MessageConf.INSERT_FAIL);
//    }
//
//    @ApiOperation(value = "通过文章Uid更新Solr索引", notes = "通过文章Uid更新Solr索引", response = String.class)
//    @PostMapping("/updateSolrIndexByUid")
//    public String updateSolrIndexByUid(@RequestParam(required = true) String uid) {
//        String result = webFeignClient.getBlogByUid(uid);
//        Blog blog = WebUtils.getData(result, Blog.class);
//        if (blog == null) {
//            return ResultUtil.result(SysConf.ERROR, MessageConf.UPDATE_FAIL);
//        }
//        solrSearchService.updateIndex(collection, blog);
//        return ResultUtil.result(SysConf.SUCCESS, MessageConf.UPDATE_SUCCESS);
//    }
//
//    @ApiOperation(value = "通过文章uid删除Solr文章索引", notes = "通过文章uid删除Solr文章索引", response = String.class)
//    @PostMapping("/deleteSolrIndexByUid")
//    public String deleteSolrIndexByUid(@RequestParam(required = true) String uid) {
//        solrSearchService.deleteIndex(collection, uid);
//        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
//    }
//
//    @ApiOperation(value = "通过uids删除Solr文章索引", notes = "通过uids删除Solr文章索引", response = String.class)
//    @PostMapping("/deleteSolrIndexByUids")
//    public String deleteSolrIndexByUids(@RequestParam(required = true) String uids) {
//
//        List<String> uidList = StringUtils.changeStringToString(uids, SysConf.FILE_SEGMENTATION);
//
//        solrSearchService.deleteBatchIndex(collection, uidList);
//
//        return ResultUtil.result(SysConf.SUCCESS, MessageConf.DELETE_SUCCESS);
//    }
//
//    @ApiOperation(value = "Solr初始化索引", notes = "Solr初始化索引", response = String.class)
//    @PostMapping("/initSolrIndex")
//    public String initSolrIndex() throws ParseException {
//        log.info("使用Solr初始化全文索引");
//        //清除所有索引
//        solrSearchService.deleteAllIndex(collection);
//        Long page = 1L;
//        Long row = 15L;
//        Integer size = 0;
//        do {
//            // 查询blog信息
//            String result = webFeignClient.getBlogBySearch(page, row);
//            //构建blog
//            List<Blog> blogList = WebUtils.getList(result, Blog.class);
//            size = blogList.size();
//            //存入索引库
//            solrSearchService.initIndex(collection, blogList);
//            //翻页
//            page++;
//
//        } while (size == 15);
//        return ResultUtil.result(SysConf.SUCCESS, null);
//    }
//
//}
